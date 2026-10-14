package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender forecast. */
data class TenderForecastView(
    var code: String,
    var title: String,
    var phone: String,
    var loadKg: String,
    var carrier: String,
    var currencyText: String,
    var batchSize: String,
    var marginText: String,
    var category: String,
    var countryText: String,
    var statusLabel: String,
)

fun toTenderForecastView(item: TenderForecast): TenderForecastView {
    return TenderForecastView(
        code = item.code,
        title = truncate(item.title, 64),
        phone = item.phone,
        loadKg = "%.2f".format(item.loadKg),
        carrier = truncate(item.carrier, 120),
        currencyText = item.currency,
        batchSize = item.batchSize.toString(),
        marginText = "%.2f".format(item.margin),
        category = item.category,
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderForecastViews(items: MutableList<TenderForecast>): MutableList<TenderForecastView> {
    val views = mutableListOf<TenderForecastView>()
    for (item in items) {
        views.add(toTenderForecastView(item))
    }
    return views
}

fun sumTenderForecastView(view: TenderForecastView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedLoadKg + parsedBatchSize.toDouble() + parsedMargin
}
