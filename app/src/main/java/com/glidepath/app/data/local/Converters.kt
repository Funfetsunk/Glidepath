package com.glidepath.app.data.local

import androidx.room.TypeConverter
import com.glidepath.app.domain.model.GoalType

/** Room type converters for enums stored as their string name. */
class Converters {
    @TypeConverter
    fun goalTypeToString(type: GoalType): String = type.name

    @TypeConverter
    fun stringToGoalType(value: String): GoalType = GoalType.valueOf(value)
}
