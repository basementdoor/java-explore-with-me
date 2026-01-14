package ru.practicum.explore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.explore.dto.HitDto;
import ru.practicum.explore.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {

    private final RestClient restClient;
    private final String appName;
    private final DateTimeFormatter formatter;

    public StatsClient(RestClient statsRestClient,
                       String appName,
                       DateTimeFormatter statsDateTimeFormatter) {
        this.restClient = statsRestClient;
        this.appName = appName;
        this.formatter = statsDateTimeFormatter;
    }

    public ResponseEntity<HitDto> hit(HttpServletRequest request) {
        HitDto hitDto = HitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app(appName)
                .build();

        return restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitDto)
                .retrieve()
                .toEntity(HitDto.class);
    }

    public ResponseEntity<List<StatsDto>> getStats(LocalDateTime start,
                                                   LocalDateTime end,
                                                   List<String> uris,
                                                   Boolean unique) {

        validateDates(start, end);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", formatter.format(start))
                .queryParam("end", formatter.format(end));

        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", String.join(",", uris));
        }
        if (unique != null) {
            uriBuilder.queryParam("unique", unique);
        }

        return restClient.get()
                .uri(uriBuilder.build().toUriString())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<StatsDto>>() {});
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Нужно указать дату начала и окончания");
        }
        if (start.isAfter(end)) {
            throw new ValidationException("Нужно указать дату начала до даты окончания");
        }
    }
}