package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone tier. */
data class ZoneTierView(
    var codeText: String,
    var labelText: String,
    var segment: String,
    var attempts: String,
    var quantityText: String,
    var visible: String,
    var summaryText: String,
    var driverId: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toZoneTierView(item: ZoneTier): ZoneTierView {
    return ZoneTierView(
        codeText = truncate(item.code, 64),
        labelText = truncate(item.label, 120),
        segment = item.segment,
        attempts = item.attempts.toString(),
        quantityText = item.quantity.toString(),
        visible = "${item.visible}",
        summaryText = item.summary,
        driverId = item.driverId,
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneTierViews(items: MutableList<ZoneTier>): MutableList<ZoneTierView> {
    val views = mutableListOf<ZoneTierView>()
    for (item in items) {
        views.add(toZoneTierView(item))
    }
    return views
}

fun sumZoneTierView(view: ZoneTierView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedAttempts.toDouble() + parsedQuantity.toDouble()
}
