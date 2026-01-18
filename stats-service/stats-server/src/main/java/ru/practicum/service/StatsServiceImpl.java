package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    @Override
    public HitDto saveHit(HitDto hitDto) {
        Hit hit = HitMapper.toHit(hitDto);
        return HitMapper.toHitDto(statsRepository.save(hit));
    }

    @Override
    public Collection<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        boolean isUrisEmpty = (uris == null || uris.isEmpty());

        List<String> safeUris = isUrisEmpty ? List.of("") : uris;

        if (unique) {
            return statsRepository.findUniqueStats(start, end, safeUris, isUrisEmpty);
        } else return statsRepository.findStats(start, end, safeUris, isUrisEmpty);
    }
}
