package com.example.sqlitebasics;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sqlitebasics.FeedReaderContract.FeedEntry;

public class MainActivity extends AppCompatActivity {

    private FeedReaderDbHelper dbHelper;
    private EditText editTitle;
    private EditText editSubtitle;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Le "gardien" de la base, créé une seule fois
        dbHelper = new FeedReaderDbHelper(getApplicationContext());

        editTitle = findViewById(R.id.editTitle);
        editSubtitle = findViewById(R.id.editSubtitle);
        textResult = findViewById(R.id.textResult);

        findViewById(R.id.btnAdd).setOnClickListener(v -> ajouter());
        findViewById(R.id.btnFind).setOnClickListener(v -> chercher());
        findViewById(R.id.btnUpdate).setOnClickListener(v -> modifier());
        findViewById(R.id.btnDelete).setOnClickListener(v -> supprimer());
    }

    // INSERT : ajoute une ligne
    private void ajouter() {
        String title = editTitle.getText().toString().trim();
        String subtitle = editSubtitle.getText().toString().trim();
        if (title.isEmpty()) {
            textResult.setText("Saisis au moins un titre.");
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);
        long newRowId = db.insert(FeedEntry.TABLE_NAME, null, values);
        textResult.setText("Ligne ajoutée, id = " + newRowId);
    }

    // SELECT : cherche par titre (ou tout si le titre est vide)
    private void chercher() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                FeedEntry.COLUMN_NAME_TITLE,
                FeedEntry.COLUMN_NAME_SUBTITLE
        };
        String title = editTitle.getText().toString().trim();
        String selection = null;
        String[] selectionArgs = null;
        if (!title.isEmpty()) {
            selection = FeedEntry.COLUMN_NAME_TITLE + " = ?";
            selectionArgs = new String[]{ title };
        }
        String sortOrder = FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = db.query(
                FeedEntry.TABLE_NAME, projection, selection, selectionArgs,
                null, null, sortOrder);

        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            String t = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE));
            String s = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_SUBTITLE));
            sb.append(id).append(" | ").append(t).append(" | ").append(s).append("\n");
        }
        cursor.close();
        textResult.setText(sb.length() == 0 ? "Aucun résultat." : sb.toString());
    }

    // UPDATE : change le sous-titre des lignes qui ont ce titre
    private void modifier() {
        String title = editTitle.getText().toString().trim();
        String subtitle = editSubtitle.getText().toString().trim();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);
        String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { title };
        int count = db.update(FeedEntry.TABLE_NAME, values, selection, selectionArgs);
        textResult.setText(count + " ligne(s) modifiée(s).");
    }

    // DELETE : supprime les lignes qui ont ce titre
    private void supprimer() {
        String title = editTitle.getText().toString().trim();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { title };
        int deletedRows = db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
        textResult.setText(deletedRows + " ligne(s) supprimée(s).");
    }

    // Fermer la base quand l'activité est détruite
    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
