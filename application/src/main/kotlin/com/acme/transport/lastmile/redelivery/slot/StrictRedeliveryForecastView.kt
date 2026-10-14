package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict redelivery forecast. */
data class StrictRedeliveryForecastView(
    var code: String,
    var labelText: String,
    var localeText: String,
    var routeCode: String,
    var reviewer: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toStrictRedeliveryForecastView(item: StrictRedeliveryForecast): StrictRedeliveryForecastView {
    return StrictRedeliveryForecastView(
        code = item.code,
        labelText = item.label,
        localeText = item.locale,
        routeCode = item.routeCode,
        reviewer = truncate(item.reviewer, 40),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRedeliveryForecastViews(items: MutableList<StrictRedeliveryForecast>): MutableList<StrictRedeliveryForecastView> {
    val views = mutableListOf<StrictRedeliveryForecastView>()
    for (item in items) {
        views.add(toStrictRedeliveryForecastView(item))
    }
    return views
}

fun sumStrictRedeliveryForecastView(view: StrictRedeliveryForecastView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
