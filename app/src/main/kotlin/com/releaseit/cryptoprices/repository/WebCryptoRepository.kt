package com.releaseit.cryptoprices.repository

import com.releaseit.cryptoprices.web.CryptoResponse
import com.releaseit.cryptoprices.web.CryptoWebService
import io.reactivex.Single

/**
 * Created by jurajbegovac on 12/02/2018.
 */
class WebCryptoRepository(private val webService: CryptoWebService) : CryptoRepository {

  override fun getCryptos(currency: Currency, limit: String): Single<List<Crypto>> =
    webService.getCryptos(currency.name, limit)
      .map { it.map { fromResponseToCrypto(it, currency) } }

  override fun getCrypto(id: String, currency: Currency): Single<Crypto> =
    webService.getCrypto(id, currency.name)
      .map { fromResponseToCrypto(it, currency) }

  /**
   * Convert from web response to crypto depending on currency
   */
  private fun fromResponseToCrypto(cryptoResponse: CryptoResponse, currency: Currency): Crypto {
    val price = when (currency) {
                  Currency.USD -> cryptoResponse.priceUsd
                  Currency.EUR -> cryptoResponse.priceEur
                  Currency.CNY -> cryptoResponse.priceCny
                } ?: ""
    val volume24h = when (currency) {
                       Currency.USD -> cryptoResponse._24hVolumeUsd
                       Currency.EUR -> cryptoResponse._24hVolumeEur
                       Currency.CNY -> cryptoResponse._24hVolumeCny
                     } ?: ""
    return Crypto(cryptoResponse.id,
                  cryptoResponse.name,
                  cryptoResponse.rank,
                  cryptoResponse.symbol,
                  price,
                  volume24h,
                  cryptoResponse.priceBtc,
                  cryptoResponse.percentChange1h ?: "",
                  cryptoResponse.percentChange24h ?: "",
                  cryptoResponse.percentChange7d ?: "",
                  cryptoResponse.availableSupply ?: "",
                  cryptoResponse.totalSupply ?: ""
                 )
  }
}
