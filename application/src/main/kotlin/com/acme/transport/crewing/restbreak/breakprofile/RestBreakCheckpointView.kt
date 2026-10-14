package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break checkpoint. */
data class RestBreakCheckpointView(
    var codeText: String,
    var title: String,
    var expiresAt: String,
    var reviewer: String,
    var laneText: String,
    var minimum: String,
    var contactEmailText: String,
    var quantityText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toRestBreakCheckpointView(item: RestBreakCheckpoint): RestBreakCheckpointView {
    return RestBreakCheckpointView(
        codeText = item.code,
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        reviewer = item.reviewer,
        laneText = truncate(item.lane, 80),
        minimum = "%.2f".format(item.minimum),
        contactEmailText = truncate(item.contactEmail, 120),
        quantityText = item.quantity.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakCheckpointViews(items: MutableList<RestBreakCheckpoint>): MutableList<RestBreakCheckpointView> {
    val views = mutableListOf<RestBreakCheckpointView>()
    for (item in items) {
        views.add(toRestBreakCheckpointView(item))
    }
    return views
}

fun sumRestBreakCheckpointView(view: RestBreakCheckpointView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedExpiresAt.toDouble() + parsedMinimum + parsedQuantity.toDouble()
}
