package com.ventthos.todo_list_app.db.Daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ventthos.todo_list_app.db.dataclasses.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM user")
    fun getAllUsers() : List<User>

    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: Int): User?

    @Delete
    fun deleteUser(user: User)

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): User?

    @Query("UPDATE user SET lastPage = :lastPage WHERE id = :userId")
    fun updateLastPage(userId: Int, lastPage: Int)

}