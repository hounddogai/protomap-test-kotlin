package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell template. */
data class DwellTemplateView(
    var code: String,
    var name: String,
    var minimumText: String,
    var quantity: String,
    var startsAt: String,
    var category: String,
    var externalRef: String,
    var vehicleId: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toDwellTemplateView(item: DwellTemplate): DwellTemplateView {
    return DwellTemplateView(
        code = truncate(item.code, 120),
        name = item.name,
        minimumText = "%.2f".format(item.minimum),
        quantity = item.quantity.toString(),
        startsAt = item.startsAt.toString(),
        category = item.category,
        externalRef = item.externalRef,
        vehicleId = item.vehicleId,
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellTemplateViews(items: MutableList<DwellTemplate>): MutableList<DwellTemplateView> {
    val views = mutableListOf<DwellTemplateView>()
    for (item in items) {
        views.add(toDwellTemplateView(item))
    }
    return views
}

fun sumDwellTemplateView(view: DwellTemplateView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMinimum + parsedQuantity.toDouble() + parsedStartsAt.toDouble()
}
