package com.vdevcode.guardian.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vdevcode.guardian.models.AppUser
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.models.Command


interface AppDAO<T : BaseModel> {

    @Insert
    fun insert(model: T)

    @Delete
    fun delete(model: T)

    @Update
    fun update(model: T)

    fun all(): LiveData<MutableList<T>>

}

@Dao
interface UserDAO : AppDAO<AppUser> {
    @Query("SELECT * FROM users")
    override fun all(): LiveData<MutableList<AppUser>>
}


@Dao
interface CommandDAO : AppDAO<Command> {
    @Query("SELECT * FROM commands")
    override fun all(): LiveData<MutableList<Command>>
}