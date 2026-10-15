package com.fit2x.app;

import android.content.Context;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;

@CapacitorPlugin(name = "HealthSteps")
public class HealthStepsPlugin extends Plugin {

    @PluginMethod
    public void getTodaySteps(PluginCall call) {
        Context context = getContext();
        int availability = HealthConnectClient.getSdkStatus(context);

        if (availability != HealthConnectClient.SDK_AVAILABLE) {
            JSObject ret = new JSObject();
            ret.put("steps", 0);
            ret.put("available", false);
            ret.put("reason", "Health Connect not available (status=" + availability + ")");
            call.resolve(ret);
            return;
        }

        HealthConnectClient client = HealthConnectClient.getOrCreate(context);

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime startOfDay = LocalDate.now(zone).atStartOfDay(zone);
        ZonedDateTime now = ZonedDateTime.now(zone);

        Set<AggregateMetric<Long>> metrics = new HashSet<>();
        metrics.add(StepsRecord.COUNT_TOTAL);
        AggregateRequest request = new AggregateRequest(
            metrics,
            TimeRangeFilter.between(startOfDay.toInstant(), now.toInstant()),
            new HashSet<>()
        );

        CompletableFuture<Long> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                AggregationResult result = BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (CoroutineScope scope, Continuation<? super AggregationResult> cont) ->
                        client.aggregate(request, cont)
                );
                Long steps = result.get(StepsRecord.COUNT_TOTAL);
                future.complete(steps != null ? steps : 0L);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).start();

        future.whenComplete((steps, err) -> {
            JSObject ret = new JSObject();
            if (err != null) {
                ret.put("steps", 0);
                ret.put("available", false);
                ret.put("reason", err.getMessage());
            } else {
                ret.put("steps", steps);
                ret.put("available", true);
            }
            call.resolve(ret);
        });
    }

    @PluginMethod
    public void isAvailable(PluginCall call) {
        int status = HealthConnectClient.getSdkStatus(getContext());
        JSObject ret = new JSObject();
        ret.put("available", status == HealthConnectClient.SDK_AVAILABLE);
        ret.put("status", status);
        call.resolve(ret);
    }
}
