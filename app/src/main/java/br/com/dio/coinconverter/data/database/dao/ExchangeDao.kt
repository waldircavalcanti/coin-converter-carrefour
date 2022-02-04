package br.com.dio.coinconverter.data.database.dao

import androidx.room.*
import br.com.dio.coinconverter.data.model.Coin
import br.com.dio.coinconverter.data.model.ExchangeResponseValue
import kotlinx.coroutines.flow.Flow


@Dao
interface ExchangeDao {

    @Query("SELECT * FROM tb_exchange ORDER BY id DESC")
    fun findAll(): Flow<List<ExchangeResponseValue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: ExchangeResponseValue)


    @Delete
    suspend fun delete(businessCard: ExchangeResponseValue)
}