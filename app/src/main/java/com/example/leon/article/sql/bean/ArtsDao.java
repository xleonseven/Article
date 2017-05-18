package com.example.leon.article.sql.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ARTS".
*/
public class ArtsDao extends AbstractDao<Arts, Long> {

    public static final String TABLENAME = "ARTS";

    /**
     * Properties of entity Arts.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Author = new Property(1, String.class, "author", false, "AUTHOR");
        public final static Property Title = new Property(2, String.class, "title", false, "userArts");
        public final static Property Content = new Property(3, String.class, "content", false, "CONTENT");
        public final static Property Time = new Property(4, String.class, "time", false, "TIME");
    }


    public ArtsDao(DaoConfig config) {
        super(config);
    }
    
    public ArtsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ARTS\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"AUTHOR\" TEXT UNIQUE ," + // 1: author
                "\"userArts\" TEXT," + // 2: title
                "\"CONTENT\" TEXT," + // 3: content
                "\"TIME\" TEXT);"); // 4: time
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ARTS\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Arts entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(2, author);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(3, title);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(4, content);
        }
 
        String time = entity.getTime();
        if (time != null) {
            stmt.bindString(5, time);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Arts entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(2, author);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(3, title);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(4, content);
        }
 
        String time = entity.getTime();
        if (time != null) {
            stmt.bindString(5, time);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Arts readEntity(Cursor cursor, int offset) {
        Arts entity = new Arts( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // author
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // title
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // content
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // time
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Arts entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAuthor(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTitle(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setContent(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTime(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Arts entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Arts entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Arts entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
