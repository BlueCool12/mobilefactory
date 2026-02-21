package mobilefactory.infrastructure.persistence.mapper;

import mobilefactory.infrastructure.persistence.entity.LottoNumberEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface LottoNumberMapper {
    Optional<LottoNumberEntity> findByParticipantId(Long participantId);

    void save(LottoNumberEntity lottoNumber);
}
