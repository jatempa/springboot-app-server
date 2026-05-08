package com.mystore.app.repository;

import com.mystore.app.entity.Client;
import com.mystore.app.entity.ClientSegment;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class ClientSpecification {

    public static Specification<Client> withFilters(
            String firstName, String lastName, String email,
            String city, String segment, Integer regionId, String regionName) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (firstName != null && !firstName.isBlank())
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            if (lastName != null && !lastName.isBlank())
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            if (email != null && !email.isBlank())
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            if (city != null && !city.isBlank())
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            if (segment != null && !segment.isBlank())
                predicates.add(cb.equal(root.get("segment"), ClientSegment.valueOf(segment.toUpperCase())));

            if (regionId != null || (regionName != null && !regionName.isBlank())) {
                var regionJoin = root.join("region", JoinType.LEFT);
                if (regionId != null)
                    predicates.add(cb.equal(regionJoin.get("regionId"), regionId));
                if (regionName != null && !regionName.isBlank())
                    predicates.add(cb.like(cb.lower(regionJoin.get("regionName")), "%" + regionName.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
