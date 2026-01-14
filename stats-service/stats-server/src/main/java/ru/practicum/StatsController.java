package ru.practicum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final StatsService statsService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public HitDto saveHit(@Valid @RequestBody HitDto hitDto) {
        return statsService.saveHit(hitDto);
    }

    @GetMapping("/stats")
    public Collection<StatsDto> getStats(@RequestParam @DateTimeFormat(pattern = PATTERN)
                                  LocalDateTime start,
                                         @RequestParam @DateTimeFormat(pattern = PATTERN)
                                  LocalDateTime end,
                                         @RequestParam(required = false) List<String> uris,
                                         @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        return statsService.getStats(start, end, uris, unique);
    }

}
