package com.releaseit.cryptoprices.utils

interface ClickableListItem<T> {
  val onClick: (T) -> (Unit)
}
