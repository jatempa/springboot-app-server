package com.mystore.app.repository;

import com.mystore.app.entity.Category;
import com.mystore.app.entity.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFiltersAndCursor(
            String sku, String productName, String categoryName,
            String sortBy, boolean ascending,
            String cursorValue, Integer cursorId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Use fetch join for main query to avoid N+1; plain join for count query
            Join<Product, Category> categoryJoin;
            if (Long.class.equals(query.getResultType())) {
                categoryJoin = root.join("category", JoinType.LEFT);
            } else {
                @SuppressWarnings("unchecked")
                Join<Product, Category> fetchedJoin = (Join<Product, Category>) (Object) root.fetch("category", JoinType.LEFT);
                categoryJoin = fetchedJoin;
            }

            // Filters
            if (sku != null && !sku.isBlank())
                predicates.add(cb.like(cb.lower(root.get("sku")), "%" + sku.toLowerCase() + "%"));
            if (productName != null && !productName.isBlank())
                predicates.add(cb.like(cb.lower(root.get("productName")), "%" + productName.toLowerCase() + "%"));
            if (categoryName != null && !categoryName.isBlank())
                predicates.add(cb.like(cb.lower(categoryJoin.get("categoryName")), "%" + categoryName.toLowerCase() + "%"));

            // Keyset cursor condition
            if (cursorValue != null && cursorId != null) {
                predicates.add(buildCursorPredicate(sortBy, ascending, cursorValue, cursorId, root, categoryJoin, cb));
            }

            // ORDER BY — Spring Data strips this from the generated count query
            if (!Long.class.equals(query.getResultType())) {
                query.orderBy(buildOrderBy(sortBy, ascending, root, categoryJoin, cb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate buildCursorPredicate(
            String sortBy, boolean ascending, String cursorValue, Integer cursorId,
            Root<Product> root, Join<Product, Category> categoryJoin, CriteriaBuilder cb) {

        if ("createdAt".equals(sortBy)) {
            String[] parts = cursorValue.split(":");
            Instant cursorInstant = Instant.ofEpochSecond(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
            if (ascending) {
                return cb.or(
                    cb.greaterThan(root.get("createdAt"), cursorInstant),
                    cb.and(cb.equal(root.get("createdAt"), cursorInstant), cb.greaterThan(root.get("productId"), cursorId))
                );
            } else {
                return cb.or(
                    cb.lessThan(root.get("createdAt"), cursorInstant),
                    cb.and(cb.equal(root.get("createdAt"), cursorInstant), cb.lessThan(root.get("productId"), cursorId))
                );
            }
        }

        Expression<String> sortExpr = switch (sortBy) {
            case "productName" -> root.get("productName");
            case "categoryName" -> categoryJoin.get("categoryName");
            default -> root.get("sku");
        };

        if (ascending) {
            return cb.or(
                cb.greaterThan(sortExpr, cursorValue),
                cb.and(cb.equal(sortExpr, cursorValue), cb.greaterThan(root.get("productId"), cursorId))
            );
        } else {
            return cb.or(
                cb.lessThan(sortExpr, cursorValue),
                cb.and(cb.equal(sortExpr, cursorValue), cb.lessThan(root.get("productId"), cursorId))
            );
        }
    }

    private static List<Order> buildOrderBy(
            String sortBy, boolean ascending,
            Root<Product> root, Join<Product, Category> categoryJoin, CriteriaBuilder cb) {

        Expression<?> primary = switch (sortBy) {
            case "productName" -> root.get("productName");
            case "categoryName" -> categoryJoin.get("categoryName");
            case "createdAt" -> root.get("createdAt");
            default -> root.get("sku");
        };

        Order primaryOrder = ascending ? cb.asc(primary) : cb.desc(primary);
        Order secondaryOrder = ascending ? cb.asc(root.get("productId")) : cb.desc(root.get("productId"));
        return List.of(primaryOrder, secondaryOrder);
    }
}
