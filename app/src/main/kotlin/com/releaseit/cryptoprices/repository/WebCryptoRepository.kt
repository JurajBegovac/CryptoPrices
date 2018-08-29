package com.releaseit.cryptoprices.repository

import com.releaseit.cryptoprices.web.CryptoResponse
import com.releaseit.cryptoprices.web.CryptoWebService
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by jurajbegovac on 12/02/2018.
 */
@Singleton
class WebCryptoRepository @Inject constructor(private val webService: CryptoWebService) :
  CryptoRepository {

  override fun getCryptos(currency: Currency, limit: String): Single<List<Crypto>> =
    webService.getCryptos(currency.name, limit)
      .map { it.map { fromResponseToCrypto(it, currency) } }

  override fun getCrypto(id: String, currency: Currency): Single<Crypto> =
    webService.getCrypto(id, currency.name)
      .map { fromResponseToCrypto(it.first(), currency) }

  /**
   * Convert from web response to crypto depending on currency
   */
  private fun fromResponseToCrypto(cryptoResponse: CryptoResponse, currency: Currency) =
    Crypto(cryptoResponse.id,
           cryptoResponse.name,
           cryptoResponse.rank,
           cryptoResponse.symbol,
           cryptoResponse.price(currency),
           cryptoResponse.volume24h(currency),
           cryptoResponse.priceBtc,
           cryptoResponse.percentChange1h ?: "",
           cryptoResponse.percentChange24h ?: "",
           cryptoResponse.percentChange7d ?: "",
           cryptoResponse.availableSupply ?: "",
           cryptoResponse.totalSupply ?: "",
           currency,
           cryptoResponse.marketCap(currency))


  private fun CryptoResponse.price(currency: Currency) = when (currency) {
                                                           Currency.USD -> priceUsd
                                                           Currency.EUR -> priceEur
                                                           Currency.CNY -> priceCny
                                                         } ?: ""

  private fun CryptoResponse.volume24h(currency: Currency) = when (currency) {
                                                               Currency.USD -> _24hVolumeUsd
                                                               Currency.EUR -> _24hVolumeEur
                                                               Currency.CNY -> _24hVolumeCny
                                                             } ?: ""

  private fun CryptoResponse.marketCap(currency: Currency) = when (currency) {
                                                               Currency.USD -> marketCapUsd
                                                               Currency.EUR -> marketCapEur
                                                               Currency.CNY -> marketCapCny
                                                             } ?: ""
}
