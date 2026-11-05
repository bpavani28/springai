package com.example.chatbot.tool;

import com.example.chatbot.tool.entity.Driver;
import com.example.chatbot.tool.entity.Load;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
public class LoadBusinessTools {

    private final LoadRepository loadRepository;

    public LoadBusinessTools(LoadRepository loadRepository) {
        this.loadRepository = loadRepository;
    }

    public Map<String, Object> getLoadStatus(Long loadId) {

        Load load = loadRepository.findById(loadId).orElseThrow(() ->
                        new IllegalArgumentException("Load not found: " + loadId));
        return Map.of(
                "loadId", load.getLoadId(),
                "customerId", load.getCustomerId(),
                "status", load.getStatus(),
                "origin", load.getOrigin(),
                "destination", load.getDestination()
        );
    }

    public List<Map<String, Object>> getDelayedLoads(String customerId) {

        return loadRepository.findDelayedLoads(
                        customerId, LocalDateTime.now())
                .stream()
                .map(load -> Map.<String, Object>of(
                        "loadId", load.getLoadId(),
                        "status", load.getStatus(),
                        "scheduledDelivery",
                        load.getScheduledDelivery().toString()
                )).toList();
    }

    public Map<String, Object> getAssignedDriver(Long loadId) {

        Load load = loadRepository.findById(loadId).orElseThrow(() ->
                        new IllegalArgumentException("Load not found: " + loadId));

        Driver driver = load.getDriver();
        if (driver == null) {
            return Map.of(
                    "loadId", loadId,
                    "driverAssigned", false);
        }
        return Map.of(
                "loadId", loadId,
                "driverAssigned", true,
                "driverId", driver.getDriverId(),
                "driverName", driver.getDriverName()
        );
    }


    public List<Map<String, Object>> getToolDefinitions() {

        return List.of(
                createTool(
                        "get_load_status",
                        "Get the current status of a load",
                        "loadId",
                        "integer"
                ),
                createTool(
                        "get_delayed_loads",
                        "Get delayed loads for a customer",
                        "customerId",
                        "string"
                ),
                createTool(
                        "get_assigned_driver",
                        "Get the assigned driver for a load",
                        "loadId",
                        "integer"
                )
        );
    }

    private Map<String, Object> createTool(
            String name,
            String description,
            String parameterName,
            String parameterType) {

        return Map.of(
                "type", "function",
                "name", name,
                "description", description,
                "strict", true,
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                parameterName, Map.of(
                                        "type", parameterType
                                )
                        ),
                        "required", List.of(parameterName),
                        "additionalProperties", false
                )
        );
    }


    public Object executeTool(String toolName, JsonNode arguments) {

        return switch (toolName) {

            case "get_load_status" -> {

                Long loadId = arguments.path("loadId").asLong();
                // ACTUAL JAVA TOOL EXECUTION
                yield getLoadStatus(loadId);
            }

            case "get_delayed_loads" -> {

                String customerId = arguments.path("customerId").asText();
                // ACTUAL JAVA TOOL EXECUTION
                yield getDelayedLoads(customerId);
            }

            case "get_assigned_driver" -> {

                Long loadId = arguments.path("loadId").asLong();
                // ACTUAL JAVA TOOL EXECUTION
                yield getAssignedDriver(loadId);
            }

            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }


}


