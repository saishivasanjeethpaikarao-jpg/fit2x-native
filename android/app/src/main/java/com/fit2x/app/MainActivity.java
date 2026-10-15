package com.fit2x.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(HealthStepsPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
