package com.glidepath.app.di

import android.content.Context
import androidx.room.Room
import com.glidepath.app.data.local.GlidepathDatabase
import com.glidepath.app.data.local.GoalDao
import com.glidepath.app.data.local.PaymentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GlidepathDatabase =
        Room.databaseBuilder(context, GlidepathDatabase::class.java, GlidepathDatabase.NAME)
            .build()

    @Provides
    fun provideGoalDao(db: GlidepathDatabase): GoalDao = db.goalDao()

    @Provides
    fun providePaymentDao(db: GlidepathDatabase): PaymentDao = db.paymentDao()
}
