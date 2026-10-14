package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered dispute forecast. */
data class TieredDisputeForecastView(
    var code: String,
    var name: String,
    var channel: String,
    var countryText: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toTieredDisputeForecastView(item: TieredDisputeForecast): TieredDisputeForecastView {
    return TieredDisputeForecastView(
        code = item.code,
        name = truncate(item.name, 64),
        channel = item.channel,
        countryText = item.country,
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDisputeForecastViews(items: MutableList<TieredDisputeForecast>): MutableList<TieredDisputeForecastView> {
    val views = mutableListOf<TieredDisputeForecastView>()
    for (item in items) {
        views.add(toTieredDisputeForecastView(item))
    }
    return views
}

fun sumTieredDisputeForecastView(view: TieredDisputeForecastView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedWindowDays.toDouble()
}
