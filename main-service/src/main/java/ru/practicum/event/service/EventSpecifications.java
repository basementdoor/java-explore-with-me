package ru.practicum.event.service;

import jakarta.persistence.criteria.Expression;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
public final class EventSpecifications {

    public static Specification<Event> isPublished() {
        return (root, query, cb) ->
                cb.equal(root.get("state"), EventState.PUBLISHED);
    }

    public static Specification<Event> textSearch(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + text.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> inCategories(List<Long> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return cb.conjunction();
            }

            return root.get("category").get("id").in(categories);
        };
    }

    public static Specification<Event> paid(Boolean paid) {
        return (root, query, cb) -> {
            if (paid == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("paid"), paid);
        };
    }

    public static Specification<Event> eventDateBetween(
            LocalDateTime start, LocalDateTime end) {

        return (root, query, cb) -> {
            if (start == null && end == null) {
                return cb.conjunction();
            }
            if (start == null) {
                return cb.lessThanOrEqualTo(root.get("eventDate"), end);
            }
            if (end == null) {
                return cb.greaterThanOrEqualTo(root.get("eventDate"), start);
            }

            return cb.between(root.get("eventDate"), start, end);
        };
    }

    public static Specification<Event> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, cb) -> {
            if (onlyAvailable == null || !onlyAvailable) {
                return cb.conjunction();
            }

            Expression<Integer> limit = root.get("participantLimit");
            Expression<Integer> confirmed = root.get("confirmedRequests");

            return cb.or(
                    cb.isNull(limit),
                    cb.equal(limit, 0),
                    cb.lessThan(confirmed, limit)
            );
        };
    }

    public static Specification<Event> initiatedByUsers(List<Long> users) {
        return (root, query, cb) -> {
            if (users == null || users.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("initiator").get("id").in(users);
        };
    }

    public static Specification<Event> inStates(List<EventState> states) {
        return (root, query, cb) -> {
            if (states == null || states.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("state").in(states);
        };
    }


}

