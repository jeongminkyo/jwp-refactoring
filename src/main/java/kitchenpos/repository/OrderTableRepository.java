package kitchenpos.repository;

import kitchenpos.domain.OrderTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderTableRepository extends JpaRepository<OrderTable, Long> {
    Long countByIdIn(List<Long> ids);
}