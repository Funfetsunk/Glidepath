package com.glidepath.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.GoalType

/** Room row for a goal. Money fields are pennies. Maps to/from the domain [Goal]. */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: GoalType,
    val total: Long,
    val currency: String,
    val monthlyTarget: Long,
    val createdAt: Long,
    val completedAt: Long?,
)

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    name = name,
    type = type,
    total = total,
    currency = currency,
    monthlyTarget = monthlyTarget,
    createdAt = createdAt,
    completedAt = completedAt,
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    name = name,
    type = type,
    total = total,
    currency = currency,
    monthlyTarget = monthlyTarget,
    createdAt = createdAt,
    completedAt = completedAt,
)
