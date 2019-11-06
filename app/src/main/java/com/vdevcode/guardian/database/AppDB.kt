package com.vdevcode.guardian.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vdevcode.guardian.models.AppUser
import com.vdevcode.guardian.models.Command

@Database(entities = arrayOf(AppUser::class, Command::class), version = 1, exportSchema = false)
abstract class AppDB : RoomDatabase() {

    abstract fun getUserDAO(): UserDAO
    abstract fun getWordDAO(): CommandDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getDB(context: Context): AppDB {
            val tempDB = INSTANCE
            tempDB?.let {
                return it;
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(context, AppDB::class.java, "guardian_db").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}