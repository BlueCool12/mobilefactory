package mobilefactory.infrastructure.persistence.mapper;

import mobilefactory.infrastructure.persistence.entity.EventEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface EventMapper {
    Optional<EventEntity> findActiveEvent(@Param("now") LocalDateTime now);

    void insert(EventEntity event);
}
