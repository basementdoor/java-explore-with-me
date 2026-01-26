package ru.practicum.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.dto.StatsDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {

    final CompilationRepository compilationRepository;
    final EventRepository eventRepository;
    final StatsClient statsClient;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        var pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("id"));

        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findByPinned(pinned, pageable);
        }
        return compilations.stream()
                .map(compilation -> CompilationMapper.toCompilationDto(compilation, getEventDto(compilation.getEvents())))
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = throwIfCompilationNotExist(compId);
        return CompilationMapper.toCompilationDto(compilation, getEventDto(compilation.getEvents()));
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events.addAll(eventRepository.findAllById(newCompilationDto.getEvents()));
        }
        Compilation newCompilation = CompilationMapper.toCompilation(newCompilationDto, events);
        return CompilationMapper.toCompilationDto(compilationRepository.save(newCompilation), getEventDto(events));
    }

    @Override
    @Transactional
    public void deleteCompilationById(Long compId) {
        throwIfCompilationNotExist(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationById(Long compId, UpdateCompilationRequest updateDto) {
        Compilation compilation = throwIfCompilationNotExist(compId);
        Set<Event> events = new HashSet<>();
        if (updateDto.getEvents() != null && !updateDto.getEvents().isEmpty()) {
            events.addAll(eventRepository.findAllById(updateDto.getEvents()));
        }
        Compilation updatedCompilation = CompilationMapper.updateCompilation(compilation, updateDto, events);
        return CompilationMapper.toCompilationDto(updatedCompilation, getEventDto(events));
    }

    private Compilation throwIfCompilationNotExist(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с ID: %s не найдена".formatted(compId)));
    }

    private List<EventShortDto> getEventDto(Set<Event> eventSet) {
        if (eventSet.isEmpty()) {
            return List.of();
        } else {
            List<Event> events = eventSet.stream().toList();
            var views = getEventsViews(events);
            return events.stream()
                    .map(event -> EventMapper.toEventShortDto(event, views.getOrDefault(event.getId(), 0L)))
                    .toList();
        }
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime start = LocalDateTime.now().minusYears(10);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        List<StatsDto> stats = statsClient.getStats(start, end, uris, true).getBody();
        if (stats == null || stats.isEmpty()) {
            return Map.of();
        }

        return stats.stream()
                .collect(Collectors.toMap(
                        s -> extractEventId(s.getUri()),
                        StatsDto::getHits,
                        Long::sum
                ));
    }

    private Long extractEventId(String uri) {
        return Long.parseLong(uri.split("/")[2]);
    }
}
