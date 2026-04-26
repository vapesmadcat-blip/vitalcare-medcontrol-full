package com.vitalcare.medcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

public class AlarmActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        String med = getIntent().getStringExtra("med");
        String dose = getIntent().getStringExtra("dose");
        if (med == null) med = "Medicamento";
        if (dose == null) dose = "";

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40,40,40,40);
        layout.setBackgroundColor(Color.rgb(7,17,31));

        TextView title = new TextView(this);
        title.setText("💊 Hora da dose");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        TextView msg = new TextView(this);
        msg.setText(med + "\n" + dose);
        msg.setTextSize(22);
        msg.setTextColor(Color.WHITE);
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0,30,0,30);

        Button ok = new Button(this);
        ok.setText("OK");
        ok.setTextSize(18);
        ok.setOnClickListener(v -> finish());

        layout.addView(title);
        layout.addView(msg);
        layout.addView(ok);
        setContentView(layout);
    }
}
