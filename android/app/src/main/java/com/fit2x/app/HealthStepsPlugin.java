package com.fit2x.app;

import android.content.Context;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.request.ReadRecordsRequest;
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
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;

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

        // Today midnight to now
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime startOfDay = LocalDate.now(zone).atStartOfDay(zone);
        ZonedDateTime now = ZonedDateTime.now(zone);

        CoroutineScope scope = new CoroutineScope(Dispatchers.getIO());
        // Use bridge to run coroutine — simplified via runOnUiThread alternative:
        // We'll use a simple thread here for brevity
        new Thread(() -> {
            try {
                AggregateRequest request = new AggregateRequest(
                    Collections.singleton(StepsRecord.COUNT_TOTAL),
                    TimeRangeFilter.between(startOfDay.toInstant(), now.toInstant())
                );
                // aggregate is a suspend function; use runBlocking
                long steps = kotlinx.coroutines.BuildersKt.runBlocking(
                    (scope, continuation) -> client.aggregate(request, continuation),
                    Long.class
                );
                JSObject ret = new JSObject();
                ret.put("steps", steps);
                ret.put("available", true);
                call.resolve(ret);
            } catch (Exception e) {
                JSObject ret = new JSObject();
                ret.put("steps", 0);
                ret.put("available", false);
                ret.put("reason", e.getMessage());
                call.resolve(ret);
            }
        }).start();
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
