package com.releaseit.cryptoprices.web

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * Created by jurajbegovac on 12/02/2018.
 */

interface CryptoWebService {

  /**
   * Fetch cryptos.
   * Return just raw response because we have different currencies so we will parse it depending on currency
   */
  @GET("ticker/")
  fun getCryptos(@Query("convert") convert: String, @Query("limit") limit: String): Single<List<CryptoResponse>>

  /**
   * Fetch crypto with more details.
   * Return just raw response because we have different currencies so we will parse it depending on currency
   */
  @GET("ticker/{id}")
  fun getCrypto(@Path("id") id: String, @Query("convert") convert: String): Single<CryptoResponse>
}

data class CryptoResponse(
  @SerializedName("id")
  @Expose
  val id: String,
  @SerializedName("name")
  @Expose
  val name: String,
  @SerializedName("symbol")
  @Expose
  val symbol: String,
  @SerializedName("rank")
  @Expose
  val rank: String,
  @SerializedName("price_usd")
  @Expose
  val priceUsd: String? = null,
  @SerializedName("price_btc")
  @Expose
  val priceBtc: String,
  @SerializedName("24h_volume_usd")
  @Expose
  val _24hVolumeUsd: String? = null,
  @SerializedName("market_cap_usd")
  @Expose
  val marketCapUsd: String? = null,
  @SerializedName("available_supply")
  @Expose
  val availableSupply: String? = null,
  @SerializedName("total_supply")
  @Expose
  val totalSupply: String? = null,
  @SerializedName("percent_change_1h")
  @Expose
  val percentChange1h: String? = null,
  @SerializedName("percent_change_24h")
  @Expose
  val percentChange24h: String? = null,
  @SerializedName("percent_change_7d")
  @Expose
  val percentChange7d: String? = null,
  @SerializedName("price_eur")
  @Expose
  val priceEur: String? = null,
  @SerializedName("24h_volume_eur")
  @Expose
  val _24hVolumeEur: String? = null,
  @SerializedName("market_cap_eur")
  @Expose
  val marketCapEur: String? = null,
  @SerializedName("price_cny")
  @Expose
  val priceCny: String? = null,
  @SerializedName("24h_volume_cny")
  @Expose
  val _24hVolumeCny: String? = null,
  @SerializedName("market_cap_cny")
  @Expose
  val marketCapCny: String? = null)
