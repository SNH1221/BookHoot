package database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database (entities = [BookEntity::class], version = 1)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao() : BookDao


    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getInstance(context: Context): BookDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BookDatabase::class.java,
                        "book-db"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}