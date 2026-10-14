package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional parcel window. */
data class RegionalParcelWindowView(
    var codeText: String,
    var titleText: String,
    var source: String,
    var quantity: String,
    var retriesText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toRegionalParcelWindowView(item: RegionalParcelWindow): RegionalParcelWindowView {
    return RegionalParcelWindowView(
        codeText = item.code,
        titleText = item.title,
        source = truncate(item.source, 80),
        quantity = item.quantity.toString(),
        retriesText = item.retries.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalParcelWindowViews(items: MutableList<RegionalParcelWindow>): MutableList<RegionalParcelWindowView> {
    val views = mutableListOf<RegionalParcelWindowView>()
    for (item in items) {
        views.add(toRegionalParcelWindowView(item))
    }
    return views
}

fun sumRegionalParcelWindowView(view: RegionalParcelWindowView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedQuantity.toDouble() + parsedRetries.toDouble() + parsedDurationMinutes.toDouble()
}
