package br.com.dio.coinconverter.data.repository

import br.com.dio.coinconverter.data.model.ExchangeResponseValue
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    suspend fun getExchangeValue(coins:String): Flow<ExchangeResponseValue>

    //Room
    suspend fun save(exchangeResponseValue: ExchangeResponseValue)
    fun list():Flow<List<ExchangeResponseValue>>
    suspend fun delete(exchangeResponseValue: ExchangeResponseValue)

}