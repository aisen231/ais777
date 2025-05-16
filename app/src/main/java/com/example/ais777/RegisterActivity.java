package com.example.ais777;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ais777.data.DBHelper;

public class RegisterActivity extends AppCompatActivity {
    private EditText etLogin, etPassword, etConfirm;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = DBHelper.getInstance(this);
        etLogin   = findViewById(R.id.etLogin);
        etPassword= findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        Button btn = findViewById(R.id.btnRegister);

        btn.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String login = etLogin.getText().toString().trim();
        String pwd   = etPassword.getText().toString();
        String cfm   = etConfirm.getText().toString();

        if (login.isEmpty() || pwd.isEmpty() || cfm.isEmpty()) {
            Toast.makeText(this, "Все поля обязательны", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pwd.equals(cfm)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = db.registerUser(login, pwd);
        if (ok) {
            Toast.makeText(this, "Успешная регистрация", Toast.LENGTH_SHORT).show();
            // просто закрываем экран регистрации —
            // вернёмся к LoginActivity, чтобы юзер там сразу ввёл свои данные
            finish();
        } else {
            Toast.makeText(this, "Логин занят или ошибка БД", Toast.LENGTH_SHORT).show();
        }
    }
}