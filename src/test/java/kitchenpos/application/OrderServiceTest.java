package kitchenpos.application;

import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuRepository;
import kitchenpos.order.application.OrderService;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItemRepository;
import kitchenpos.order.domain.OrderRepository;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.product.domain.Product;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("주문 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderTableRepository orderTableRepository;

    @InjectMocks
    private OrderService orderService;

    @DisplayName("주문을 신청한다.")
    @Test
    void create1() {
        //given
        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);

        given(orderRepository.save(any()))
                .willReturn(new Order(OrderStatus.COOKING, LocalDateTime.now()));

        Menu menu1 = new Menu("메뉴1", new BigDecimal(16000), null);
        Menu menu2 = new Menu("메뉴2", new BigDecimal(16000), null);
        ReflectionTestUtils.setField(menu1, "id", 1L);
        ReflectionTestUtils.setField(menu2, "id", 2L);
        given(menuRepository.findAllById(Arrays.asList(1L, 2L)))
                .willReturn(Arrays.asList(menu1, menu2));

        OrderTable orderTable1 = new OrderTable(3, false);
        ReflectionTestUtils.setField(orderTable1, "id", 1L);
        given(orderTableRepository.findById(1L))
                .willReturn(Optional.of(orderTable1));

        OrderTable orderTable = new OrderTable(3, false);
        ReflectionTestUtils.setField(orderTable, "id", 2L);

        Order order = new Order(OrderStatus.COOKING, LocalDateTime.now());
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setOrderTable(orderTable);

        given(orderRepository.save(any())).willReturn(order);

        //when
        List<OrderLineItemRequest> orderLineItems = new ArrayList<>();
        orderLineItems.add(new OrderLineItemRequest(1L, 1));
        orderLineItems.add(new OrderLineItemRequest(2L, 1));

        OrderRequest orderRequest = new OrderRequest(1L, OrderStatus.MEAL);
        orderRequest.setOrderLineItems(orderLineItems);
        OrderResponse createOrder = orderService.create(orderRequest);

        //then
        verify(orderRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getId()).isNull();
        assertThat(argumentCaptor.getValue().getOrderStatus()).isEqualTo(OrderStatus.COOKING);
        assertThat(argumentCaptor.getValue().getOrderTable().getId()).isEqualTo(1L);

        assertThat(createOrder.getId()).isEqualTo(1L);
        assertThat(createOrder.getOrderTableId()).isEqualTo(2L);
        assertThat(createOrder.getOrderStatus()).isEqualTo(OrderStatus.COOKING);
    }


    @DisplayName("주문을 신청한다 - 주문 항목에 아무것도 없으면 주문할 수 없다.")
    @Test
    void create2() {
        //given
        OrderRequest newOrder = new OrderRequest();
        newOrder.setOrderLineItems(Collections.EMPTY_LIST);
        //when
        //then
        assertThatThrownBy(() -> orderService.create(newOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목이 비어있습니다.");
    }

    @DisplayName("주문을 신청한다 - 주문 항목에 등록하지 않은 메뉴가 있다면 주문할 수 없다.")
    @Test
    void create3() {
        //given
        Menu menu = new Menu( "메뉴2", new BigDecimal(16000), null);
        ReflectionTestUtils.setField(menu, "id", 2L);
        given(menuRepository.findAllById(Arrays.asList(1L, 2L)))
                .willReturn(Collections.singletonList(menu));

        List<OrderLineItemRequest> orderLineItems = new ArrayList<>();
        orderLineItems.add(new OrderLineItemRequest(1L, 3));
        orderLineItems.add(new OrderLineItemRequest(2L, 3));

        OrderRequest newOrder = new OrderRequest();
        newOrder.setOrderLineItems(orderLineItems);

        //when
        //then
        assertThatThrownBy(() -> orderService.create(newOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목에 등록하지 않은 메뉴가 있습니다.");
    }

    @DisplayName("주문을 신청한다 - 주문 테이블이 없거니 비어있으면 주문할 수 없다.")
    @Test
    void create4() {
        //given
        Menu menu1 = new Menu("메뉴1", new BigDecimal(16000), null);
        Menu menu2 = new Menu("메뉴2", new BigDecimal(16000), null);
        ReflectionTestUtils.setField(menu1, "id", 1L);
        ReflectionTestUtils.setField(menu2, "id", 2L);
        given(menuRepository.findAllById(Arrays.asList(1L, 2L)))
                .willReturn(Arrays.asList(menu1, menu2));

        OrderTable orderTable = new OrderTable(0, true);
        ReflectionTestUtils.setField(orderTable, "id", 1L);
        given(orderTableRepository.findById(any()))
                .willReturn(Optional.of(orderTable));

        List<OrderLineItemRequest> orderLineItems = new ArrayList<>();
        orderLineItems.add(new OrderLineItemRequest(1L, 3));
        orderLineItems.add(new OrderLineItemRequest(2L, 3));

        OrderRequest newOrder = new OrderRequest();
        newOrder.setOrderLineItems(orderLineItems);
        //when
        //then
        assertThatThrownBy(() -> orderService.create(newOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비어있는 주문 테이블입니다.");
    }

    @DisplayName("주문 목록을 조회할 수 있다.")
    @Test
    void list() {
        //given
        Order order1 = new Order(OrderStatus.COOKING, LocalDateTime.now());
        Order order2 = new Order(OrderStatus.MEAL, LocalDateTime.now());
        ReflectionTestUtils.setField(order1, "id", 1L);
        ReflectionTestUtils.setField(order2, "id", 2L);

        given(orderRepository.findAll())
                .willReturn(Arrays.asList(order1, order2));
        //when
        List<OrderResponse> orderResponses = orderService.list();

        //then
        assertThat(orderResponses.size()).isEqualTo(2);

        assertThat(orderResponses.get(0).getId()).isEqualTo(1L);
        assertThat(orderResponses.get(0).getOrderStatus()).isEqualTo(OrderStatus.COOKING);

        assertThat(orderResponses.get(1).getId()).isEqualTo(2L);
        assertThat(orderResponses.get(1).getOrderStatus()).isEqualTo(OrderStatus.MEAL);
    }

    @DisplayName("주문 상태를 변경한다.")
    @Test
    void changeOrderStatus1() {
        //given
        Order order = new Order(OrderStatus.COOKING, LocalDateTime.now());
        ReflectionTestUtils.setField(order, "id", 1L);
        given(orderRepository.findById(any()))
                .willReturn(Optional.of(order));

        OrderRequest changeOrder = new OrderRequest();
        changeOrder.setOrderStatus(OrderStatus.MEAL);

        //when
        OrderResponse changedOrder = orderService.changeOrderStatus(1L, changeOrder);

        //then
        assertThat(changedOrder.getId()).isEqualTo(1L);
        assertThat(changedOrder.getOrderStatus()).isEqualTo(OrderStatus.MEAL);
    }

    @DisplayName("주문 상태를 변경한다. - 신청하지 않은 주문은 상태를 변경할 수 없다.")
    @Test
    void changeOrderStatus2() {
        //given
        given(orderRepository.findById(any()))
                .willReturn(Optional.empty());

        //when
        //then
        OrderRequest changeOrder = new OrderRequest();
        changeOrder.setOrderStatus(OrderStatus.MEAL);
        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, changeOrder))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 상태를 변경한다. - 이미 왼료상태인 주문은 상태를 변경할 수 없다.")
    @Test
    void changeOrderStatus3() {
        //given
        Order order = new Order(OrderStatus.COMPLETION, LocalDateTime.now());
        ReflectionTestUtils.setField(order, "id", 1L);

        given(orderRepository.findById(any()))
                .willReturn(Optional.of(order));

        OrderRequest changeOrder = new OrderRequest();
        changeOrder.setOrderStatus(OrderStatus.MEAL);

        //when
        //then
        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, changeOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 왼료상태인 주문은 상태 변경이 불가합니다.");
    }
}