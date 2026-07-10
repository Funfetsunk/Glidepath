package com.glidepath.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.glidepath.app.domain.model.Payment

/** Room row for a payment. Deleted with its goal via cascade. */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goalId")],
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Long,
    val date: Long,
    val note: String,
)

fun PaymentEntity.toDomain(): Payment = Payment(
    id = id,
    goalId = goalId,
    amount = amount,
    date = date,
    note = note,
)

fun Payment.toEntity(): PaymentEntity = PaymentEntity(
    id = id,
    goalId = goalId,
    amount = amount,
    date = date,
    note = note,
)
