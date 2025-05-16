package com.example.ais777;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ais777.data.DBHelper;
import com.example.ais777.data.Session;

public class AddPasswordActivity extends AppCompatActivity {

    private EditText etService, etLogin, etPassword, etNotes;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);

        db = DBHelper.getInstance(this);

        etService  = findViewById(R.id.etService);
        etLogin    = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        etNotes    = findViewById(R.id.etNotes);
        Button btn  = findViewById(R.id.btnSave);

        btn.setOnClickListener(v -> savePassword());
    }

    private void savePassword() {
        String srv   = etService.getText().toString().trim();
        String login = etLogin.getText().toString().trim();
        String pwd   = etPassword.getText().toString();
        String notes = etNotes.getText().toString().trim();

        if (srv.isEmpty() || login.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Сервис, логин и пароль обязательны", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = db.addPassword(
                Session.getInstance().getUserId(),
                srv, login, pwd, notes
        );
        if (ok) {
            Toast.makeText(this, "Успешно сохранено", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
        }
    }
}