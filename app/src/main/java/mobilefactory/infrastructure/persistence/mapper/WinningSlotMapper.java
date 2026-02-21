package mobilefactory.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WinningSlotMapper {
    void insertBatch(@Param("eventId") Long eventId, @Param("slots") List<?> slots);

    Integer findRankBySequence(@Param("eventId") Long eventId, @Param("sequenceNo") int sequenceNo);
}
