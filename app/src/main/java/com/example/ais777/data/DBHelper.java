package com.example.ais777.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.example.ais777.util.CryptoUtil;

import javax.crypto.SecretKey;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ais777.db";
    private static final int    DB_VERSION = 1;

    // Таблица пользователей
    private static final String T_USERS  = "users";
    private static final String U_ID     = "id";
    private static final String U_LOGIN  = "login";
    private static final String U_HASH   = "hash";
    private static final String U_SALT   = "salt";

    // Таблица паролей
    private static final String T_PW      = "passwords";
    private static final String P_ID      = "id";
    private static final String P_SERVICE = "service";
    private static final String P_LOGIN   = "login";
    private static final String P_PASS    = "password";
    private static final String P_NOTES   = "notes";
    private static final String P_USER_ID = "user_id";

    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DBHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создаём таблицу users
        db.execSQL("CREATE TABLE " + T_USERS + " (" +
                U_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                U_LOGIN + " TEXT UNIQUE, " +
                U_HASH  + " TEXT, " +
                U_SALT  + " BLOB" +
                ")");

        // Создаём таблицу passwords
        db.execSQL("CREATE TABLE " + T_PW + " (" +
                P_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                P_SERVICE + " TEXT, " +
                P_LOGIN   + " TEXT, " +
                P_PASS    + " TEXT, " +
                P_NOTES   + " TEXT, " +
                P_USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + P_USER_ID + ") REFERENCES " +
                T_USERS + "(" + U_ID + ")" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // При необходимости миграций
    }

    /**
     * Регистрирует нового пользователя.
     * @return true, если всё успешно, false — если логин занят или ошибка.
     */
    public boolean registerUser(String login, String password) {
        SQLiteDatabase db = getWritableDatabase();
        // Проверяем, нет ли уже такого логина
        Cursor cur = db.query(
                T_USERS,
                new String[]{U_ID},
                U_LOGIN + "=?",
                new String[]{login},
                null, null, null
        );
        if (cur != null && cur.moveToFirst()) {
            cur.close();
            return false;  // логин уже занят
        }
        if (cur != null) cur.close();

        try {
            // Генерируем соль и ключ по PBKDF2
            byte[] salt = CryptoUtil.generateSalt();
            SecretKey key = CryptoUtil.deriveKey(password.toCharArray(), salt);
            // Хешируем ключ в Base64
            String hash = Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);

            // Сохраняем в БД
            ContentValues cv = new ContentValues();
            cv.put(U_LOGIN, login);
            cv.put(U_HASH,  hash);
            cv.put(U_SALT,  salt);
            long id = db.insert(T_USERS, null, cv);
            return id != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Аутентифицирует пользователя.
     * @return ID пользователя (>0) или -1 при ошибке/не найден.
     */
    public int authenticate(String login, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                T_USERS,
                new String[]{U_ID, U_HASH, U_SALT},
                U_LOGIN + "=?",
                new String[]{login},
                null, null, null
        );

        int userId = -1;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    // Надёжно получаем индексы колонок
                    int idxId   = c.getColumnIndexOrThrow(U_ID);
                    int idxHash = c.getColumnIndexOrThrow(U_HASH);
                    int idxSalt = c.getColumnIndexOrThrow(U_SALT);

                    int    id         = c.getInt(idxId);
                    String storedHash = c.getString(idxHash);
                    byte[] salt       = c.getBlob(idxSalt);

                    // Восстанавливаем ключ из введённого пароля + соли
                    SecretKey key = CryptoUtil.deriveKey(password.toCharArray(), salt);
                    String hash2 = Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);

                    if (hash2.equals(storedHash)) {
                        // Успешная аутентификация
                        Session.getInstance().setUser(id, key);
                        userId = id;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return userId;
    }

    /**
     * Добавляет новую запись пароля.
     * @return true, если успешно.
     */
    public boolean addPassword(int userId, String service, String login, String pwd, String notes) {
        try {
            SecretKey key = Session.getInstance().getKey();
            String enc = CryptoUtil.encrypt(key, pwd);

            ContentValues cv = new ContentValues();
            cv.put(P_SERVICE, service);
            cv.put(P_LOGIN,   login);
            cv.put(P_PASS,    enc);
            cv.put(P_NOTES,   notes);
            cv.put(P_USER_ID, userId);

            long id = getWritableDatabase().insert(T_PW, null, cv);
            return id != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Возвращает курсор всех паролей текущего пользователя,
     * при этом столбец id алиасится в _id для CursorAdapter.
     */
    public Cursor getPasswords(int userId) {
        String sql = "SELECT "
                + P_ID      + " AS _id, "
                + P_SERVICE + ", "
                + P_LOGIN   + ", "
                + P_PASS    + ", "
                + P_NOTES
                + " FROM "   + T_PW
                + " WHERE "  + P_USER_ID + "=?";
        return getReadableDatabase().rawQuery(
                sql,
                new String[]{ String.valueOf(userId) }
        );
    }
}