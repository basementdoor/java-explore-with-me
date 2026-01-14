package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.dto.StatsDto;
import ru.practicum.explore.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("select h.app as app, h.uri as uri, count(h.id) as hits " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end " +
            "and (:urisEmpty = true or h.uri in :uris) " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<StatsDto> findStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("uris") List<String> uris,
                             @Param("urisEmpty") boolean urisEmpty);

    @Query("select h.app as app, h.uri as uri, count(distinct h.ip) as hits " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end " +
            "and (:urisEmpty = true or h.uri in :uris) " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<StatsDto> findUniqueStats(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("uris") List<String> uris,
                                              @Param("urisEmpty") boolean urisEmpty);
}
