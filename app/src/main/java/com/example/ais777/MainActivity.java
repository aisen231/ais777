package com.example.ais777;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ais777.data.DBHelper;
import com.example.ais777.data.Session;

public class MainActivity extends AppCompatActivity {

    private ListView lv;
    private SimpleCursorAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = DBHelper.getInstance(this);

        lv = findViewById(R.id.lvPasswords);
        findViewById(R.id.fabAdd)
                .setOnClickListener(v -> startActivity(
                        new Intent(this, AddPasswordActivity.class)));

        // Клик по элементу — открыть детальный просмотр
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, ViewPasswordActivity.class);
            i.putExtra("pw_id", (int) id);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPasswords();
    }

    private void loadPasswords() {
        Cursor c = db.getPasswords(Session.getInstance().getUserId());
        if (c.getCount() == 0) {
            Toast.makeText(this, "Сохранённых паролей ещё нет", Toast.LENGTH_SHORT).show();
        }
        // из таблицы passwords: columns service, login, _id
        String[] from = new String[]{"service", "login"};
        int[]    to   = new int[]{R.id.tvService, R.id.tvLogin};

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item_password,
                c,
                from,
                to,
                0
        );
        lv.setAdapter(adapter);
    }
}