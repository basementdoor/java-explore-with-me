package ru.practicum.compilation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        return List.of();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return null;
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilation) {
        return null;
    }

    @Override
    @Transactional
    public void deleteCompilationById(Long compId) {

    }

    @Override
    @Transactional
    public CompilationDto updateCompilationById(Long compId, UpdateCompilationRequest updateCompilation) {
        return null;
    }
}
