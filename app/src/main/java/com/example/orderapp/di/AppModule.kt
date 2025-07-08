package com.example.orderapp.di

import android.content.Context
import com.example.orderapp.model.AppDatabase
import com.example.orderapp.model.CategoryDao
import com.example.orderapp.model.DataTransferManager
import com.example.orderapp.model.OrderHistoryDao
import com.example.orderapp.model.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao {
        return db.productDao()
    }

    @Singleton
    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao {
        return db.categoryDao()
    }

    @Singleton
    @Provides
    fun provideOrderHistoryDao(db: AppDatabase): OrderHistoryDao {
        return db.orderHistoryDao()
    }

    @Singleton
    @Provides
    fun provideDataTransferManager(@ApplicationContext context: Context): DataTransferManager {
        return DataTransferManager(context)
    }
}