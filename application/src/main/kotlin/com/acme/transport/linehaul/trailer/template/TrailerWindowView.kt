package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer window. */
data class TrailerWindowView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var externalRefText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toTrailerWindowView(item: TrailerWindow): TrailerWindowView {
    return TrailerWindowView(
        code = truncate(item.code, 64),
        titleText = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        externalRefText = item.externalRef,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerWindowViews(items: MutableList<TrailerWindow>): MutableList<TrailerWindowView> {
    val views = mutableListOf<TrailerWindowView>()
    for (item in items) {
        views.add(toTrailerWindowView(item))
    }
    return views
}

fun sumTrailerWindowView(view: TrailerWindowView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedFuelLiters + parsedUpdatedAt.toDouble()
}
