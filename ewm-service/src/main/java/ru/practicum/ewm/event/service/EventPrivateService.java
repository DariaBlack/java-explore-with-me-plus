package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.location.service.LocationService;
import ru.practicum.ewm.mapper.EwmMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventPrivateService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final EwmMapper mapper;

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        log.info("Создание события пользователем userId={}", userId);

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата начала события должна быть минимум через 2 часа от текущего момента");
        }

        Event event = mapper.toEvent(dto,
                categoryService.getCategoryById(dto.getCategory()),
                locationService.getOrCreateLocation(dto.getLocation()),
                initiator);

        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Event saved = eventRepository.save(event);
        return mapper.toEventFullDto(saved, 0L, 0L);
    }

    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        log.info("Обновление события пользователем userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не может изменять чужое событие");
        }
        if (!(event.getState() == EventState.PENDING || event.getState() == EventState.CANCELED)) {
            throw new ConflictException("Изменять можно только события в состоянии ожидания или отмененные");
        }
        if (dto.getEventDate() != null &&
                dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть минимум через 2 часа");
        }

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getCategory() != null) event.setCategory(categoryService.getCategoryById(dto.getCategory()));
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) event.setLocation(locationService.getOrCreateLocation(dto.getLocation()));
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT -> event.setState(EventState.PENDING);
                case REJECT_EVENT -> event.setState(EventState.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        return mapper.toEventFullDto(updated, 0L, 0L);
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        log.info("Получение событий пользователя userId={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size))
                .map(mapper::toEventShortDto)
                .toList();
    }

    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Получение события пользователем userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не может просматривать чужое событие");
        }

        return mapper.toEventFullDto(event, 0L, 0L);
    }
}
