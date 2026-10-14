package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary idling checkpoint. */
data class PrimaryIdlingCheckpointView(
    var code: String,
    var name: String,
    var timezone: String,
    var notes: String,
    var externalRef: String,
    var discount: String,
    var rateText: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toPrimaryIdlingCheckpointView(item: PrimaryIdlingCheckpoint): PrimaryIdlingCheckpointView {
    return PrimaryIdlingCheckpointView(
        code = item.code,
        name = item.name,
        timezone = item.timezone,
        notes = item.notes,
        externalRef = truncate(item.externalRef, 64),
        discount = "%.2f".format(item.discount),
        rateText = "%.2f".format(item.rate),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryIdlingCheckpointViews(items: MutableList<PrimaryIdlingCheckpoint>): MutableList<PrimaryIdlingCheckpointView> {
    val views = mutableListOf<PrimaryIdlingCheckpointView>()
    for (item in items) {
        views.add(toPrimaryIdlingCheckpointView(item))
    }
    return views
}

fun sumPrimaryIdlingCheckpointView(view: PrimaryIdlingCheckpointView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedDiscount + parsedRate + parsedGraceDays.toDouble()
}
