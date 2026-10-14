package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop profile. */
data class DropProfileView(
    var code: String,
    var label: String,
    var quantityText: String,
    var active: String,
    var minimum: String,
    var currency: String,
    var score: String,
    var revision: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toDropProfileView(item: DropProfile): DropProfileView {
    return DropProfileView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 120),
        quantityText = item.quantity.toString(),
        active = "${item.active}",
        minimum = "%.2f".format(item.minimum),
        currency = item.currency,
        score = "%.2f".format(item.score),
        revision = item.revision.toString(),
        routeCode = truncate(item.routeCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropProfileViews(items: MutableList<DropProfile>): MutableList<DropProfileView> {
    val views = mutableListOf<DropProfileView>()
    for (item in items) {
        views.add(toDropProfileView(item))
    }
    return views
}

fun sumDropProfileView(view: DropProfileView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedQuantity.toDouble() + parsedMinimum + parsedScore
}
