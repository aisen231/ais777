package com.example.ais777;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ais777.data.DBHelper;
import com.example.ais777.data.Session;
import com.example.ais777.util.CryptoUtil;

public class ViewPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_password);

        TextView tvSrv  = findViewById(R.id.tvService);
        TextView tvLog  = findViewById(R.id.tvLoginFull);
        TextView tvPwd  = findViewById(R.id.tvPasswordFull);
        TextView tvNotes= findViewById(R.id.tvNotes);

        int pwId = getIntent().getIntExtra("pw_id", -1);
        if (pwId == -1) {
            finish();
            return;
        }

        Cursor c = DBHelper.getInstance(this).getReadableDatabase().query(
                "passwords", null, "id=?",
                new String[]{String.valueOf(pwId)},
                null, null, null
        );

        if (c.moveToFirst()) {
            String srv   = c.getString(c.getColumnIndex("service"));
            String login = c.getString(c.getColumnIndex("login"));
            String enc   = c.getString(c.getColumnIndex("password"));
            String notes = c.getString(c.getColumnIndex("notes"));
            c.close();
            try {
                String dec = CryptoUtil.decrypt(
                        Session.getInstance().getKey(), enc
                );
                tvSrv.setText(srv);
                tvLog.setText("Логин: " + login);
                tvPwd.setText("Пароль: " + dec);
                tvNotes.setText("Заметки: " + notes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            c.close();
        }
    }
}