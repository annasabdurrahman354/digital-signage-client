package com.ppwb.digitalsignage.di

import android.app.Application
import androidx.room.Room
import com.ppwb.digitalsignage.data.local.SignageDatabase
import com.ppwb.digitalsignage.data.local.dao.SignageDao
import com.ppwb.digitalsignage.data.remote.SignageApi
import com.ppwb.digitalsignage.data.repository.SignageRepositoryImpl
import com.ppwb.digitalsignage.domain.repository.SignageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSignageApi(): SignageApi {
        return Retrofit.Builder()
            .baseUrl("http://digisign.test/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SignageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): SignageDatabase {
        return Room.databaseBuilder(
            app,
            SignageDatabase::class.java,
            "signage_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSignageDao(db: SignageDatabase): SignageDao = db.signageDao

    @Provides
    @Singleton
    fun provideRepository(api: SignageApi, dao: SignageDao): SignageRepository {
        return SignageRepositoryImpl(api, dao)
    }
}
