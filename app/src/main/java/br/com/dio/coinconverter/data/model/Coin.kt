package br.com.dio.coinconverter.data.model

import java.util.*

enum class Coin(val locale: Locale) {
    USD(Locale.US),
    CAD(Locale.CANADA),
    BRL(Locale("pt", "BR")),
    ARS(Locale("es", "ARS")),
    GBP(Locale("es", "GBP")),
    ILS(Locale("iw", "ILS")),
    EUR(Locale("en", "EUR"));

    companion object {
        fun getByName(name: String) = values().find { it.name == name } ?: BRL
    }

}