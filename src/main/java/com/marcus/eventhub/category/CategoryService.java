package com.marcus.eventhub.category;

import com.marcus.eventhub.category.dto.CategoryResponse;
import com.marcus.eventhub.category.dto.CreateCategoryRequest;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public Category getByIdOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Category name already exists");
        }
        if (categoryRepository.existsBySlugIgnoreCase(request.slug())) {
            throw new BusinessException("Category slug already exists");
        }

        Category category = categoryRepository.save(new Category(request.name(), request.slug()));
        return CategoryResponse.from(category);
    }
}
