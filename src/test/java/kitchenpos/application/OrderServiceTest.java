package kitchenpos.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderLineItemDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;

@DisplayName("주문 BO 테스트")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private MenuDao menuDao;
	@Mock
	private OrderDao orderDao;
	@Mock
	private OrderLineItemDao orderLineItemDao;
	@Mock
	private OrderTableDao orderTableDao;

	@InjectMocks
	private OrderService orderService;

	private Menu 메뉴1;
	private Menu 메뉴2;
	private OrderTable 주문_테이블;

	@BeforeEach
	void setUp() {
		메뉴1 = new Menu();
		메뉴1.setId(1L);
		메뉴1.setPrice(BigDecimal.valueOf(10000L));
		메뉴2 = new Menu();
		메뉴2.setId(2L);
		메뉴2.setPrice(BigDecimal.valueOf(20000L));
		주문_테이블 = new OrderTable();
		주문_테이블.setId(1L);
	}

	@DisplayName("주문 생성")
	@Test
	void create_happyPath() {
		// given
		OrderLineItem 주문_항목1 = new OrderLineItem();
		주문_항목1.setMenuId(메뉴1.getId());
		주문_항목1.setQuantity(1);
		OrderLineItem 주문_항목2 = new OrderLineItem();
		주문_항목2.setMenuId(메뉴2.getId());
		주문_항목2.setQuantity(2);
		Order 주문 = new Order();
		주문.setOrderLineItems(Arrays.asList(주문_항목1, 주문_항목2));
		주문.setOrderTableId(주문_테이블.getId());

		given(menuDao.countByIdIn(Arrays.asList(메뉴1.getId(), 메뉴2.getId()))).willReturn(2L);
		given(orderTableDao.findById(주문_테이블.getId())).willReturn(Optional.of(주문_테이블));
		given(orderDao.save(주문)).willAnswer(invocation -> {
			주문.setId(1L);
			return 주문;
		});

		// when
		주문_항목2.setMenuId(메뉴2.getId());
		Order saveOrder = orderService.create(주문);

		// then
		assertThat(saveOrder.getId()).isEqualTo(1L);
	}

	@DisplayName("주문 생성 : 주문 항목의 메뉴는 중복")
	@Test
	void create_exceptionCase1() {
		// given
		OrderLineItem 주문_항목1 = new OrderLineItem();
		주문_항목1.setMenuId(메뉴1.getId());
		주문_항목1.setQuantity(1);
		OrderLineItem 주문_항목2 = new OrderLineItem();
		주문_항목2.setMenuId(메뉴1.getId());
		주문_항목2.setQuantity(2);
		Order 주문 = new Order();
		주문.setOrderLineItems(Arrays.asList(주문_항목1, 주문_항목2));
		주문.setOrderTableId(주문_테이블.getId());

		given(menuDao.countByIdIn(Arrays.asList(메뉴1.getId(), 메뉴1.getId()))).willReturn(1L);

		// when & then
		assertThatThrownBy(() -> orderService.create(주문)).isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("주문 생성 : 주문 항목이 비어있음")
	@Test
	void create_exceptionCase2() {
		// given
		Order 주문 = new Order();
		주문.setOrderLineItems(Collections.EMPTY_LIST);

		// when & then
		assertThatThrownBy(() -> orderService.create(주문)).isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("주문 생성 : 주문 테이블 번호가 비어있음")
	@Test
	void create_exceptionCase3() {
		// given
		OrderLineItem 주문_항목1 = new OrderLineItem();
		주문_항목1.setMenuId(메뉴1.getId());
		주문_항목1.setQuantity(1);
		Order 주문 = new Order();
		주문.setOrderLineItems(Arrays.asList(주문_항목1));

		given(menuDao.countByIdIn(Arrays.asList(메뉴1.getId()))).willReturn(1L);

		// when & then
		assertThatThrownBy(() -> orderService.create(주문)).isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("주문 목록 조회")
	@Test
	void list() {
		// given
		Order 주문 = new Order();
		주문.setId(1L);
		주문.setOrderTableId(주문_테이블.getId());
		given(orderDao.findAll()).willReturn(Arrays.asList(주문));

		OrderLineItem 주문_항목1 = new OrderLineItem();
		OrderLineItem 주문_항목2 = new OrderLineItem();
		given(orderLineItemDao.findAllByOrderId(주문.getId())).willReturn(Arrays.asList(주문_항목1, 주문_항목2));

		// when
		List<Order> saveOrderList = orderService.list();

		// then
		assertThat(saveOrderList).hasSize(1);
		assertThat(saveOrderList.get(0).getId()).isEqualTo(1L);
		assertThat(saveOrderList.get(0).getOrderTableId()).isEqualTo(주문_테이블.getId());
		assertThat(saveOrderList.get(0).getOrderLineItems()).contains(주문_항목1, 주문_항목2);
	}

	@DisplayName("주문 변경 : 성공케이스")
	@Test
	void changeOrderStatus_happyPath() {
		// given
		Order 주문 = new Order();
		주문.setId(1L);
		주문.setOrderStatus(OrderStatus.COOKING.name());
		given(orderDao.findById(주문.getId())).willReturn(Optional.of(주문));

		OrderLineItem 주문_항목1 = new OrderLineItem();
		OrderLineItem 주문_항목2 = new OrderLineItem();
		given(orderLineItemDao.findAllByOrderId(주문.getId())).willReturn(Arrays.asList(주문_항목1, 주문_항목2));

		// when
		Order 임시 = new Order();
		임시.setOrderStatus(OrderStatus.MEAL.name());
		Order saveOrder = orderService.changeOrderStatus(주문.getId(), 임시);

		// then
		assertThat(saveOrder.getOrderStatus()).isEqualTo(OrderStatus.MEAL.name());
	}

	@DisplayName("주문 변경: 이미 계산 완료 상태인 주문을 변경 시도함")
	@Test
	void changeOrderStatus_exceptionCase() {
		// given
		Order 주문 = new Order();
		주문.setId(1L);
		주문.setOrderStatus(OrderStatus.COMPLETION.name());
		given(orderDao.findById(주문.getId())).willReturn(Optional.of(주문));

		// when & then
		Order 임시 = new Order();
		임시.setOrderStatus(OrderStatus.MEAL.name());
		assertThatThrownBy(() -> orderService.changeOrderStatus(주문.getId(), 임시))
			.isInstanceOf(IllegalArgumentException.class);
	}
}