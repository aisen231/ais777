package com.example.ais777;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ais777.data.DBHelper;

public class LoginActivity extends AppCompatActivity {
    private EditText etLogin, etPassword;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        db = DBHelper.getInstance(this);
        etLogin    = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);

        Button btn = findViewById(R.id.btnLogin);
        btn.setOnClickListener(v -> doLogin());

        TextView tvReg = findViewById(R.id.tvToRegister);
        tvReg.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void doLogin() {
        String login = etLogin.getText().toString().trim();
        String pwd   = etPassword.getText().toString();
        if (login.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Заполните оба поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int uid = db.authenticate(login, pwd);
        if (uid > 0) {
            // успешный вход — переходим на главный экран
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
        }
    }
}