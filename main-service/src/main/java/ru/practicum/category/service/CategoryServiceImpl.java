package ru.practicum.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {

    final CategoryRepository categoryRepository;
    final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        int page = from / size;
        return categoryRepository.findAll(PageRequest.of(page, size, Sort.by("id")))
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с ID: %s не найдена".formatted(categoryId)));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        checkIfCategoryNameExist(newCategoryDto.getName());
        Category newCategory = CategoryMapper.toCategory(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(newCategory));
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) {
        throwIfCategoryNotExist(categoryId);
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Для категории с ID %s существуют события".formatted(categoryId));
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, NewCategoryDto updateCategoryDto) {
        Category category = throwIfCategoryNotExist(categoryId);
        if (!category.getName().equals(updateCategoryDto.getName())) {
            checkIfCategoryNameExist(updateCategoryDto.getName());
            category.setName(updateCategoryDto.getName());
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    private Category throwIfCategoryNotExist(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с ID: %s не найдена".formatted(categoryId)));
    }

    private void checkIfCategoryNameExist(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("Категория с именем %s уже существует".formatted(name));
        }
    }
}
