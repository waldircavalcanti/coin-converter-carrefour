package br.com.dio.coinconverter.domain.di

import br.com.dio.coinconverter.domain.DeleteExchangeUseCase
import br.com.dio.coinconverter.domain.GetExchangeValueUseCase
import br.com.dio.coinconverter.domain.ListExchangeUseCase
import br.com.dio.coinconverter.domain.SaveExchangeUseCase
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

object DomainModule {

    fun load(){
        loadKoinModules(userCaseModules())
    }

    private fun userCaseModules(): Module {
        return module {
            factory { GetExchangeValueUseCase(get()) }
            factory { ListExchangeUseCase(get()) }
            factory { DeleteExchangeUseCase(get()) }
            factory { SaveExchangeUseCase(get()) }
        }
    }
}