package ru.practicum.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;
import ru.practicum.event.EventState;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Setter
@Getter
@ToString
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    String annotation;

    @ManyToOne
    @JoinColumn(name = "category")
    Category category;

    @Column(name = "confirmed_requests")
    int confirmedRequests = 0;

    @Column(name = "created_on")
    LocalDateTime createdOn = LocalDateTime.now();

    String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator", nullable = false)
    User initiator;

    float lat;

    float lon;

    boolean paid;

    @Column(name = "participant_limit")
    Integer participantLimit = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    EventState state = EventState.PENDING;

    @Column(nullable = false)
    String title;
}
