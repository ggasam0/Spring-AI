package com.example.springai.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class ToolService {

    private final Map<String, String> orderStatus = new ConcurrentHashMap<>();
    private final Map<Integer, String> tickets = new ConcurrentHashMap<>();
    private final AtomicInteger ticketCounter = new AtomicInteger(1000);
    private final Map<String, String> storeHours = Map.of(
            "shanghai", "09:00-21:00",
            "beijing", "10:00-22:00",
            "shenzhen", "09:30-20:30"
    );

    public ToolService() {
        orderStatus.put("123", "已发货，预计 2 天内送达");
        orderStatus.put("456", "处理中，预计明天出库");
        orderStatus.put("789", "已签收");
    }

    public String execute(String tool, Map<String, String> arguments) {
        return switch (tool) {
            case "getOrderStatus" -> getOrderStatus(arguments.get("orderId"));
            case "createTicket" -> createTicket(arguments.get("issue"), arguments.get("contact"));
            case "getStoreHours" -> getStoreHours(arguments.get("store"));
            default -> "未识别的工具: " + tool;
        };
    }

    public String getOrderStatus(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return "缺少订单号";
        }
        return orderStatus.getOrDefault(orderId, "未找到订单 " + orderId);
    }

    public String createTicket(String issue, String contact) {
        int id = ticketCounter.incrementAndGet();
        String summary = "工单#" + id + " | 问题: " + safe(issue) + " | 联系方式: " + safe(contact) + " | 创建时间: " + Instant.now();
        tickets.put(id, summary);
        return summary;
    }

    public String getStoreHours(String store) {
        if (store == null) {
            return "请提供门店名称";
        }
        return storeHours.getOrDefault(store.toLowerCase(), "未找到门店: " + store);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "未提供" : value;
    }
}
