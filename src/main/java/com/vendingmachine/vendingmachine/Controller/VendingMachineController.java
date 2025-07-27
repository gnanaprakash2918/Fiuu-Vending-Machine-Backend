package com.vendingmachine.vendingmachine.Controller;

import com.vendingmachine.vendingmachine.dto.FiuuCallbackRequest;
import com.vendingmachine.vendingmachine.dto.FiuuPaymentResponse;
import com.vendingmachine.vendingmachine.model.Order;
import com.vendingmachine.vendingmachine.Service.VendingMachineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.glxn.qrgen.javase.QRCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/vending")
public class VendingMachineController {

    private final VendingMachineService vendingMachineService;

    public VendingMachineController(VendingMachineService vendingMachineService) {
        this.vendingMachineService = vendingMachineService;
    }

    @PostMapping("/selectItem/{itemId}")
    public ResponseEntity<?> selectItem(@PathVariable String itemId) {
        try {
            Order order = vendingMachineService.selectItem(itemId);
            FiuuPaymentResponse fiuuResponse = vendingMachineService.sendPaymentRequest(order);

            if ("00".equals(fiuuResponse.getStatus())) {
                return ResponseEntity.ok(fiuuResponse);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate payment URL: " + fiuuResponse.getMessage());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing payment request.");
        }
    }

    @GetMapping(value = "/generateQrCode/{orderId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@PathVariable String orderId) {
        Order order = vendingMachineService.getOrder(orderId);
        if (order == null || order.getPaymentURL() == null) {
            return ResponseEntity.notFound().build();
        }

        try (ByteArrayOutputStream bos = QRCode.from(order.getPaymentURL()).stream()) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/fiuu/callback")
    public ResponseEntity<String> fiuuCallback(@RequestBody FiuuCallbackRequest callbackRequest) {
        System.out.println("Received Fiuu callback for order: " + callbackRequest.getOrderID() +
                ", status: " + callbackRequest.getStatus());

        if ("PAID".equals(callbackRequest.getStatus())) {
            boolean updated = vendingMachineService.updateOrderStatus(callbackRequest.getOrderID(), "PAID");
            if (updated) {
                return ResponseEntity.ok("Callback procefssed successfully. Item dispensed.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found or already processed.");
            }
        } else {
            vendingMachineService.updateOrderStatus(callbackRequest.getOrderID(), callbackRequest.getStatus());
            return ResponseEntity.ok("Callback received with status: " + callbackRequest.getStatus());
        }
    }
}
