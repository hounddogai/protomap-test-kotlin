package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card forecast. */
data class RateCardForecastView(
    var code: String,
    var name: String,
    var lane: String,
    var activeText: String,
    var amountText: String,
    var category: String,
    var batchSize: String,
    var quantityText: String,
    var visible: String,
    var statusLabel: String,
)

fun toRateCardForecastView(item: RateCardForecast): RateCardForecastView {
    return RateCardForecastView(
        code = truncate(item.code, 80),
        name = item.name,
        lane = item.lane,
        activeText = "${item.active}",
        amountText = "%.2f".format(item.amount),
        category = item.category,
        batchSize = item.batchSize.toString(),
        quantityText = item.quantity.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardForecastViews(items: MutableList<RateCardForecast>): MutableList<RateCardForecastView> {
    val views = mutableListOf<RateCardForecastView>()
    for (item in items) {
        views.add(toRateCardForecastView(item))
    }
    return views
}

fun sumRateCardForecastView(view: RateCardForecastView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedAmount + parsedBatchSize.toDouble() + parsedQuantity.toDouble()
}
