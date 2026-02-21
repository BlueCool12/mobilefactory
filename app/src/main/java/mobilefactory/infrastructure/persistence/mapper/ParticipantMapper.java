package mobilefactory.infrastructure.persistence.mapper;

import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ParticipantMapper {
    int countByEventId(Long eventId);

    int countNonFirstPrizeParticipants(Long eventId);

    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber, @Param("eventId") Long eventId);

    Optional<ParticipantEntity> findByPhoneNumber(@Param("phoneNumber") String phoneNumber,
            @Param("eventId") Long eventId);

    void save(ParticipantEntity participant);

    void update(ParticipantEntity participant);

    List<ParticipantEntity> findByCheckCountAndRegisteredAtBefore(
            @Param("checkCount") int checkCount,
            @Param("registeredAt") LocalDateTime registeredAt);

    List<ParticipantEntity> findUncheckedWinnersByAnnouncementDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
