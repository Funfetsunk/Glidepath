package com.glidepath.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoal(id: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: Long): GoalEntity?

    @Query("SELECT * FROM goals ORDER BY createdAt DESC LIMIT 1")
    fun observeMostRecentGoal(): Flow<GoalEntity?>

    @Query("UPDATE goals SET completedAt = :completedAt WHERE id = :id")
    suspend fun setCompletedAt(id: Long, completedAt: Long?)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)
}
