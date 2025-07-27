package com.vendingmachine.vendingmachine.Service;

import com.vendingmachine.vendingmachine.dto.FiuuPaymentResponse;
import com.vendingmachine.vendingmachine.model.Item;
import com.vendingmachine.vendingmachine.model.Order;

import com.vendingmachine.vendingmachine.Repository.ItemRepository;
import com.vendingmachine.vendingmachine.Repository.OrderRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Service
public class VendingMachineService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    // Configuration for Fiuu API from application.properties
    @Value("${fiuu.api.url}")
    private String fiuuApiUrl;
    @Value("${fiuu.merchant.id}")
    private String fiuuMerchantId;
    @Value("${fiuu.vcode}")
    private String fiuuVcode;
    @Value("${fiuu.skey}")
    private String fiuuSkey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public VendingMachineService(RestTemplate restTemplate, ObjectMapper objectMapper,
                                 ItemRepository itemRepository, OrderRepository orderRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;

        if (itemRepository.count() == 0) {
            itemRepository.save(new Item("A01", "afdsfd", 1.00));
            itemRepository.save(new Item("A02", "fdfdfd", 1.50));
        }
    }

    @Transactional
    public Order selectItem(String itemID) {
        Optional<Item> itemOptional = itemRepository.findById(itemID);
        if (itemOptional.isEmpty()) {
            throw new IllegalArgumentException("Item not found: " + itemID);
        }
        Item item = itemOptional.get();

        String orderId = "VM001-TRX" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, itemID, item.getPrice(), "PENDING", null);

        return orderRepository.save(order);
    }

    @Transactional
    public FiuuPaymentResponse sendPaymentRequest(Order order) throws JsonProcessingException {
        String requestBody = "merchantid=" + URLEncoder.encode(fiuuMerchantId, StandardCharsets.UTF_8) +
                "&orderid=" + URLEncoder.encode(order.getOrderID(), StandardCharsets.UTF_8) +
                "&amount=" + URLEncoder.encode(String.format("%.2f", order.getAmount()), StandardCharsets.UTF_8) +
                "&vcode=" + URLEncoder.encode(fiuuVcode, StandardCharsets.UTF_8) +
                "&skey=" + URLEncoder.encode(fiuuSkey, StandardCharsets.UTF_8) +
                "&channel=" + URLEncoder.encode("E_WALLET", StandardCharsets.UTF_8);

        FiuuPaymentResponse fiuuResponse = new FiuuPaymentResponse();
        fiuuResponse.setStatus("00");
        fiuuResponse.setMessage("Success");
        fiuuResponse.setPayment_url("https://fiuu.com/payment/" + order.getOrderID());

        // Update the order with the payment URL and save to the database
        order.setPaymentURL(fiuuResponse.getPayment_url());
        orderRepository.save(order); // Save the updated order

        return fiuuResponse;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Transactional
    public boolean updateOrderStatus(String orderId, String status) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            orderRepository.save(order);

            if ("PAID".equals(status)) {
                dispenseItem(order.getItemID());
                System.out.println("Item " + order.getItemID() + " dispensed for order " + orderId);
            }
            return true;
        }
        return false;
    }

    private void dispenseItem(String itemID) {
        System.out.println("Dispensing item: " + itemID);
    }
}