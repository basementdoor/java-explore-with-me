package ru.practicum.explore.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.dto.HitDto;
import ru.practicum.explore.model.Hit;

import java.time.LocalDateTime;

@UtilityClass
public class HitMapper {

    public HitDto toHitDto(Hit hit) {
        return HitDto.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

    public Hit toHit(HitDto hitDto) {
        return Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
