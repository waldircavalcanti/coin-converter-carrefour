package br.com.dio.coinconverter.data.repository

import br.com.dio.coinconverter.core.exceptions.RemoteException
import br.com.dio.coinconverter.data.database.AppDataBase
import br.com.dio.coinconverter.data.model.ErrorResponse
import br.com.dio.coinconverter.data.model.ExchangeResponseValue
import br.com.dio.coinconverter.data.services.AwesomeService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class CoinRepositoryImpl(
    private val service: AwesomeService,
    appDataBase: AppDataBase
) : CoinRepository {

    private val dao = appDataBase.exchangeDao()


    override suspend fun getExchangeValue(coins: String) = flow {
        try {
            val exchangeValue = service.exchangeValue(coins)
            val exchange = exchangeValue.values.first()
            emit(exchange)
        } catch (e: HttpException) {
            val json = e.response()?.errorBody()?.string()

            //Convert string to object
            val errorResponse = Gson().fromJson(json, ErrorResponse::class.java)
            throw RemoteException(errorResponse.message)
        }

    }

    override suspend fun save(exchangeResponseValue: ExchangeResponseValue) {
        dao.save(exchangeResponseValue)
    }

    override fun list(): Flow<List<ExchangeResponseValue>> {
        return dao.findAll()
    }

    override suspend fun delete(exchangeResponseValue: ExchangeResponseValue) {
        dao.delete(exchangeResponseValue)
    }
}