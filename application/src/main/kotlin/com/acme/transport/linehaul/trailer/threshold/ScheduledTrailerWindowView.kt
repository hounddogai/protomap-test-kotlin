package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer window. */
data class ScheduledTrailerWindowView(
    var code: String,
    var title: String,
    var region: String,
    var odometer: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toScheduledTrailerWindowView(item: ScheduledTrailerWindow): ScheduledTrailerWindowView {
    return ScheduledTrailerWindowView(
        code = item.code,
        title = truncate(item.title, 120),
        region = truncate(item.region, 120),
        odometer = item.odometer.toString(),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerWindowViews(items: MutableList<ScheduledTrailerWindow>): MutableList<ScheduledTrailerWindowView> {
    val views = mutableListOf<ScheduledTrailerWindowView>()
    for (item in items) {
        views.add(toScheduledTrailerWindowView(item))
    }
    return views
}

fun sumScheduledTrailerWindowView(view: ScheduledTrailerWindowView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedOdometer.toDouble() + parsedQuantity.toDouble()
}
