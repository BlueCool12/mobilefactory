package mobilefactory.infrastructure.persistence.mapper;

import mobilefactory.infrastructure.persistence.entity.SmsHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsHistoryMapper {
    void save(SmsHistoryEntity smsHistory);
}
