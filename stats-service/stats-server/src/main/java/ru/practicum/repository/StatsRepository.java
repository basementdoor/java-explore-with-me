package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.StatsDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("""
        select new ru.practicum.dto.StatsDto(
            h.app,
            h.uri,
            count(h.id)
        )
        from Hit h
        where h.timestamp between :start and :end
        and (:urisEmpty = true or h.uri in :uris)
        group by h.app, h.uri
        order by count(h.id) desc
        """)
    List<StatsDto> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris,
            @Param("urisEmpty") boolean urisEmpty
    );

    @Query("""
        select new ru.practicum.dto.StatsDto(
            h.app,
            h.uri,
            count(distinct h.ip)
        )
        from Hit h
        where h.timestamp between :start and :end
        and (:urisEmpty = true or h.uri in :uris)
        group by h.app, h.uri
        order by count(distinct h.ip) desc
        """)
    List<StatsDto> findUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris,
            @Param("urisEmpty") boolean urisEmpty
    );
}
