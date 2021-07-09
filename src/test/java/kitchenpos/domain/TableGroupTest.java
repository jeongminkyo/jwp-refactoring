package kitchenpos.domain;

import static kitchenpos.domain.MenuTest.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kitchenpos.exception.IllegalOperationException;
import kitchenpos.exception.OrderNotCompletedException;

@DisplayName("테이블 그룹 단위 테스트")
public class TableGroupTest {

    @Test
    @DisplayName("테이블 그룹을 생성한다")
    void create() {
        // given
        OrderTable 테이블1 = new OrderTable(NumberOfGuests.of(2), true);
        OrderTable 테이블2 = new OrderTable(NumberOfGuests.of(2), true);

        // when
        TableGroup tableGroup = new TableGroup(OrderTables.of(테이블1, 테이블2));

        // then
        tableGroup.getOrderTables().forEach(orderTable -> {
            assertThat(orderTable.isEmpty()).isFalse();
        });
    }

    @Test
    @DisplayName("테이블 그룹 생성 실패(목록이 비어있거나 목록이 2보다 작음)")
    void create_failed1() {
        // given
        OrderTable 테이블1 = new OrderTable(NumberOfGuests.of(2), true);

        // then
        assertThatThrownBy(() -> new TableGroup(OrderTables.of(테이블1)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테이블 그룹 생성 실패(테이블이 비어있지 않거나 이미 그룹화 된 테이블)")
    void create_failed3() {
        // given
        OrderTable 테이블3 = new OrderTable(3L, NumberOfGuests.of(0), true);
        OrderTable 테이블9_사용중 = new OrderTable(9L, NumberOfGuests.of(4), false);

        // then
        assertThatThrownBy(() -> new TableGroup(1L, OrderTables.of(테이블3, 테이블9_사용중)))
            .isInstanceOf(IllegalOperationException.class);
    }

    @Test
    @DisplayName("특정 테이블 그룹을 삭제한다")
    void ungroup() {
        // given
        OrderTable 테이블3 = new OrderTable(3L, NumberOfGuests.of(0), true);
        OrderTable 테이블4 = new OrderTable(4L, NumberOfGuests.of(0), true);
        TableGroup 테이블_그룹 = new TableGroup(1L, OrderTables.of(테이블3, 테이블4));

        // when
        테이블_그룹.ungroup();

        // then
        테이블_그룹.getOrderTables().forEach(orderTable -> {
            assertThat(orderTable.hasTableGroup()).isFalse();
        });
    }

    @Test
    @DisplayName("특정 테이블 그룹 삭제가 실패한다(조리/식사 테이블이 존재)")
    void ungroup_failed() {
        // given
        OrderTable 테이블3 = new OrderTable(3L, NumberOfGuests.of(0), true);
        OrderTable 테이블4 = new OrderTable(4L, NumberOfGuests.of(0), true);
        TableGroup 테이블_그룹 = new TableGroup(1L, OrderTables.of(테이블3, 테이블4));
        테이블3.addOrder(new Order(100L, OrderStatus.COMPLETION,
            OrderLineItems.of(new OrderLineItem(후라이드_메뉴, Quantity.valueOf(1)))));
        테이블4.addOrder(new Order(200L, OrderStatus.COOKING,
            OrderLineItems.of(new OrderLineItem(양념치킨_메뉴, Quantity.valueOf(1)))));

        // then
        assertThatThrownBy(테이블_그룹::ungroup)
            .isInstanceOf(OrderNotCompletedException.class);
    }
}