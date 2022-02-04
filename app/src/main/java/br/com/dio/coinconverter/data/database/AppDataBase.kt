package br.com.dio.coinconverter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.dio.coinconverter.data.database.dao.ExchangeDao
import br.com.dio.coinconverter.data.model.ExchangeResponseValue

@Database(entities = [ExchangeResponseValue::class],version = 1)
abstract class AppDataBase: RoomDatabase(){

    //Method exposes the DAO
    abstract fun exchangeDao(): ExchangeDao

    //Create Bank Instance
    companion object {
        fun getInstance(context: Context): AppDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDataBase::class.java,
                "app_database"
            ).fallbackToDestructiveMigration().build()
        }
    }
}