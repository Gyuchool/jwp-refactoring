package kitchenpos.application;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.MenuQuantityRequest;
import kitchenpos.dto.OrderCreateRequest;
import kitchenpos.dto.OrderResponse;
import kitchenpos.dto.OrderStatusRequest;
import kitchenpos.repository.MenuGroupRepository;
import kitchenpos.repository.MenuRepository;
import kitchenpos.repository.OrderTableRepository;
import kitchenpos.repository.TableGroupRepository;

class OrderServiceTest extends ServiceTest {

    @Autowired
    private TableGroupRepository tableGroupRepository;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private MenuGroupRepository menuGroupRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderService orderService;

    private TableGroup tableGroup;

    private OrderTable orderTable;

    private OrderLineItem orderLineItem;

    private MenuQuantityRequest menuQuantityRequest;

    @BeforeEach
    void setUp() {
        tableGroup = tableGroupRepository.save(new TableGroup(LocalDateTime.now()));
        orderTable = orderTableRepository.save(new OrderTable(5, false));

        final MenuGroup menuGroup = menuGroupRepository.save(new MenuGroup("이십마리메뉴"));
        final Menu menu = menuRepository.save(new Menu("후라이드치킨", BigDecimal.valueOf(16000), menuGroup.getId()));
        orderLineItem = new OrderLineItem(null, menu.getId(), 1L);
        menuQuantityRequest = new MenuQuantityRequest(orderLineItem.getMenuId(), orderLineItem.getQuantity());
    }

    @DisplayName("create: 주문 생성")
    @Test
    void create() {
        final OrderCreateRequest orderCreateRequest = new OrderCreateRequest(orderTable.getId(), Collections.singletonList(menuQuantityRequest));
        final OrderResponse actual = orderService.create(orderCreateRequest);

        assertThat(actual).isNotNull();
        assertThat(actual.getOrderTableId()).isEqualTo(orderTable.getId());
        assertThat(actual.getOrderStatus()).isEqualTo(OrderStatus.COOKING.name());
        assertThat(actual.getOrderLineItemResponses()).isNotEmpty();
    }

    @DisplayName("create: 주문 항목이 비어있을 때 예외 처리")
    @Test
    void create_IfOrderLineItemEmpty_Exception() {
        final OrderCreateRequest orderCreateRequest = new OrderCreateRequest(orderTable.getId() , Collections.emptyList());

        assertThatThrownBy(() -> orderService.create(orderCreateRequest))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("create: 주문 항목의 메뉴와 메뉴에서 조회한 것이 다를 때 예외 처리")
    @Test
    void create_IfOrderLineItemNotSameMenus_Exception() {
        MenuQuantityRequest notSameMenu = new MenuQuantityRequest(0L, orderLineItem.getQuantity());
        final OrderCreateRequest orderCreateRequest = new OrderCreateRequest(orderTable.getId(), Collections.singletonList(notSameMenu));

        assertThatThrownBy(() -> orderService.create(orderCreateRequest))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("create: 주문 테이블이 없을 때 예외 처리")
    @Test
    void create_IfOrderTableDoesNotExist_Exception() {
        final OrderCreateRequest orderCreateRequest = new OrderCreateRequest(0L, Collections.singletonList(menuQuantityRequest));

        assertThatThrownBy(() -> orderService.create(orderCreateRequest))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("create: 주문 테이블이 비어 있을 때 예외 처리")
    @Test
    void create_IfOrderTableEmpty_Exception() {
        final OrderTable emptyOrderTable = orderTableRepository.save(new OrderTable(5, true));
        final OrderCreateRequest orderCreateRequest = new OrderCreateRequest(emptyOrderTable.getId(), Collections.singletonList(menuQuantityRequest));

        assertThatThrownBy(() -> orderService.create(orderCreateRequest))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("list: 주문 전체 조회")
    @Test
    void list() {
        final OrderCreateRequest orderCreateRequest1 = new OrderCreateRequest(orderTable.getId(), Collections.singletonList(menuQuantityRequest));
        final OrderCreateRequest orderCreateRequest2 = new OrderCreateRequest(orderTable.getId(), Collections.singletonList(menuQuantityRequest));
        orderService.create(orderCreateRequest1);
        orderService.create(orderCreateRequest2);

        final List<OrderResponse> actual = orderService.list();

        assertThat(actual).hasSize(2);
    }

    @DisplayName("changeOrderStatus: 주문 상태를 변경")
    @Test
    void changeOrderStatus() {
        final OrderResponse orderResponse = orderService.create(new OrderCreateRequest(orderTable.getId(), Collections.singletonList(menuQuantityRequest)));
        final OrderStatusRequest orderStatusRequest = new OrderStatusRequest("MEAL");

        final OrderResponse actual = orderService.changeOrderStatus(orderResponse.getId(), orderStatusRequest);

        assertThat(actual.getOrderStatus()).isEqualTo(OrderStatus.MEAL.name());
    }

    @DisplayName("changeOrderStatus: 주문을 찾을 수 없을 때 예외 처리")
    @Test
    void changeOrderStatus_IfNotFindId_Exception() {
        final OrderStatusRequest orderStatusMeal = new OrderStatusRequest("MEAL");

        assertThatThrownBy(() -> orderService.changeOrderStatus(0L, orderStatusMeal))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("changeOrderStatus: 주문 상태가 이미 완료되어 있을 때 예외 처리")
    @Test
    void changeOrderStatus_IfOrderStatusIsCompletion_Exception() {
        final OrderResponse orderResponse = orderService.create(new OrderCreateRequest(orderTable.getId(), Collections.singletonList(menuQuantityRequest)));
        final OrderStatusRequest orderStatusRequest = new OrderStatusRequest("COMPLETION");
        orderService.changeOrderStatus(orderResponse.getId(), orderStatusRequest);

        assertThatThrownBy(() -> orderService.changeOrderStatus(orderResponse.getId(), orderStatusRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}