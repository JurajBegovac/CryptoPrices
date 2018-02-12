package com.releaseit.cryptoprices.repository

import io.reactivex.Single

/**
 * Created by jurajbegovac on 12/02/2018.
 */
interface CryptoRepository {

  fun getCryptos(currency: Currency, limit: String): Single<List<Crypto>>

  fun getCrypto(id: String, currency: Currency): Single<Crypto>
}

data class Crypto(val id: String,
                  val name: String,
                  val rank: String,
                  val symbol: String,
                  val price: String,
                  val _24hVolume: String,
                  val priceBtc: String,
                  val percentChange1h: String,
                  val percentChange24h: String,
                  val percentChange7d: String,
                  val availableSupply: String,
                  val totalSupply: String)

enum class Currency {
  USD, EUR, CNY
}
