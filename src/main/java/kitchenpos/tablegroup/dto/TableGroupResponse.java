package kitchenpos.tablegroup.dto;

import kitchenpos.table.domain.OrderTable;
import kitchenpos.tablegroup.domain.TableGroup;

import java.time.LocalDateTime;
import java.util.List;

public class TableGroupResponse {
    private Long id;
    private LocalDateTime createdDate;
    private List<OrderTable> orderTables;

    protected TableGroupResponse() {
    }

    public TableGroupResponse(Long id, LocalDateTime createdDate, List<OrderTable> orderTables) {
        this.id = id;
        this.createdDate = createdDate;
        this.orderTables = orderTables;
    }

    public static TableGroupResponse from(TableGroup savedTableGroup) {
        return new TableGroupResponse(savedTableGroup.getId(),
                savedTableGroup.getCreatedDate(), savedTableGroup.getOrderTables());
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTable> getOrderTables() {
        return orderTables;
    }
}