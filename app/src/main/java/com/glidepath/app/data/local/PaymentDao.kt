package com.glidepath.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: PaymentEntity): Long

    @Update
    suspend fun update(payment: PaymentEntity)

    @Delete
    suspend fun delete(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE goalId = :goalId ORDER BY date DESC, id DESC")
    fun observePayments(goalId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE goalId = :goalId ORDER BY date DESC, id DESC")
    suspend fun getPayments(goalId: Long): List<PaymentEntity>

    @Query("DELETE FROM payments WHERE goalId = :goalId")
    suspend fun deleteForGoal(goalId: Long)
}
