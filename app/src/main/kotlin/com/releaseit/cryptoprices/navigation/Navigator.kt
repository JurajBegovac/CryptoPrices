package com.releaseit.cryptoprices.navigation

/**
 * Created by jurajbegovac on 14/02/2018.
 */

sealed class Screen {
  object CryptoList : Screen()
  data class CryptoDetails(val id: String) : Screen()
  object Settings : Screen()
}

interface Navigator {
  fun navigateTo(screen: Screen)
}
