package com.acme.transport.linehaul.tender.forecast

/** How screens and exports show a pending tender forecast. */
data class PendingTenderForecastView(
    var code: String,
    var nameText: String,
    var country: String,
    var shiftCode: String,
    var margin: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toPendingTenderForecastView(item: PendingTenderForecast): PendingTenderForecastView {
    return PendingTenderForecastView(
        code = item.code,
        nameText = item.name,
        country = item.country,
        shiftCode = item.shiftCode,
        margin = "%.2f".format(item.margin),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTenderForecastViews(items: MutableList<PendingTenderForecast>): MutableList<PendingTenderForecastView> {
    val views = mutableListOf<PendingTenderForecastView>()
    for (item in items) {
        views.add(toPendingTenderForecastView(item))
    }
    return views
}

fun sumPendingTenderForecastView(view: PendingTenderForecastView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedMargin + parsedRetries.toDouble()
}
