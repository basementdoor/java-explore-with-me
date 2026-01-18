package ru.practicum.service;

import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatsService {

    HitDto saveHit(HitDto hitDto);

    Collection<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
