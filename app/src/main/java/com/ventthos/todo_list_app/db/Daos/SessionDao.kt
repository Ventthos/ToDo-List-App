package com.ventthos.todo_list_app.db.Daos

import androidx.room.*
import com.ventthos.todo_list_app.db.dataclasses.Session

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session)

    @Query("SELECT * FROM session LIMIT 1")
    fun getActiveSession(): Session?

    @Query("DELETE FROM session")
    fun clearSession()
}