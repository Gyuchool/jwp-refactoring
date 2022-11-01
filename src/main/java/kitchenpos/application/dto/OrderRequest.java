package kitchenpos.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;

public class OrderRequest {

    public static class Create {
        private Long orderTableId;
        private List<OrderLineItemRequest.Create> orderLineItems;

        private Create() {
        }

        public Create(Long orderTableId, List<OrderLineItemRequest.Create> orderLineItems) {
            this.orderTableId = orderTableId;
            this.orderLineItems = orderLineItems;
        }

        public Long getOrderTableId() {
            return orderTableId;
        }

        public List<OrderLineItemRequest.Create> getOrderLineItems() {
            return orderLineItems;
        }

    }

    public static class Status {
        private OrderStatus orderStatus;

        public Status(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
        }

        private Status() {
        }

        public OrderStatus getOrderStatus() {
            return orderStatus;
        }
    }
}