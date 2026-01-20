package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Set;

@UtilityClass
public class CompilationMapper {

    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(events)
                .build();
    }

    public Compilation toCompilation(NewCompilationDto compilationDto, Set<Event> events) {
        return Compilation.builder()
                .pinned(compilationDto.isPinned())
                .title(compilationDto.getTitle())
                .events(events)
                .build();
    }

    public Compilation toUpdatedCompilation(Compilation compilation,
                                            UpdateCompilationRequest compilationDto,
                                            Set<Event> events) {
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getEvents() != null) {
            compilation.setEvents(events);
        }
        return compilation;
    }
}
