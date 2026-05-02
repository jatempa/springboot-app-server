package com.mystore.app.messaging;

import com.mystore.app.dto.OrderResponseDTO;
import com.mystore.app.service.OrderReportService;
import com.mystore.app.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReportConsumer {

    private final OrderService orderService;
    private final OrderReportService orderReportService;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consume(Integer orderId) {
        log.info("Received order report request for orderId={}", orderId);
        OrderResponseDTO order = orderService.findById(orderId);
        orderReportService.generateReport(order);
    }
}
