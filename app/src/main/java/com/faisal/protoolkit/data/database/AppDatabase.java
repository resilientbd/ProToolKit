package com.faisal.protoolkit.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import com.faisal.protoolkit.data.entities.DocumentEntity;
import com.faisal.protoolkit.data.entities.PageEntity;
import com.faisal.protoolkit.data.entities.FolderEntity;
import com.faisal.protoolkit.data.entities.ExportEntity;
import com.faisal.protoolkit.data.dao.DocumentDao;
import com.faisal.protoolkit.data.dao.PageDao;
import com.faisal.protoolkit.data.dao.FolderDao;
import com.faisal.protoolkit.data.dao.ExportDao;

@Database(
    entities = {DocumentEntity.class, PageEntity.class, FolderEntity.class, ExportEntity.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({}) // We'll add converters if needed
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract DocumentDao documentDao();
    public abstract PageDao pageDao();
    public abstract FolderDao folderDao();
    public abstract ExportDao exportDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "protoolkit_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}