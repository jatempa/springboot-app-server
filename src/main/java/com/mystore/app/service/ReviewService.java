package com.mystore.app.service;

import com.mystore.app.dto.PagedReviewResponseDTO;
import com.mystore.app.dto.ReviewRequestDTO;
import com.mystore.app.dto.ReviewResponseDTO;
import com.mystore.app.dto.mapper.ReviewMapper;
import com.mystore.app.entity.*;
import com.mystore.app.repository.*;
import com.mystore.app.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;

    public List<ReviewResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public PagedReviewResponseDTO findAllPaged(int pageSize, String cursor) {
        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);

        Page<Review> page;
        if (cursor == null || cursor.isBlank()) {
            page = repository.findAllOrderByReviewedAtDescReviewIdDesc(pageRequest);
        } else {
            CursorUtils.CursorData cursorData = CursorUtils.decodeCursor(cursor);
            page = repository.findByKeyset(cursorData.timestamp(), cursorData.id(), pageRequest);
        }

        List<Review> reviews = page.getContent();
        boolean hasMore = reviews.size() > pageSize;

        if (hasMore) {
            reviews = reviews.subList(0, pageSize);
        }

        String nextCursor = null;
        if (hasMore && !reviews.isEmpty()) {
            Review last = reviews.get(reviews.size() - 1);
            nextCursor = CursorUtils.encodeCursor(last.getReviewedAt(), last.getReviewId());
        }

        return new PagedReviewResponseDTO(
            reviews.stream().map(mapper::toResponse).toList(),
            nextCursor,
            hasMore,
            pageSize
        );
    }

    public ReviewResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    public List<ReviewResponseDTO> findByProductId(Integer productId) {
        return repository.findByProduct_ProductId(productId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    public List<ReviewResponseDTO> findByClientId(Integer clientId) {
        return repository.findByClient_ClientId(clientId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    public ReviewResponseDTO save(ReviewRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        Client client = clientRepository.findById(dto.getClientId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found"));

        Review entity = mapper.toEntity(dto);
        entity.setProduct(product);
        entity.setClient(client);
        entity.setReviewedAt(Instant.now());
        entity.setHelpfulVotes(0);

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public ReviewResponseDTO update(Integer id, ReviewRequestDTO dto) {
        Review entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        entity.setRating(dto.getRating());
        entity.setTitle(dto.getTitle());
        entity.setBody(dto.getBody());

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }
        repository.deleteById(id);
    }
}