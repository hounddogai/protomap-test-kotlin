package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire checkpoint. */
data class TireCheckpointView(
    var codeText: String,
    var labelText: String,
    var limitText: String,
    var timezone: String,
    var durationMinutesText: String,
    var loadKgText: String,
    var archived: String,
    var batchSize: String,
    var cost: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toTireCheckpointView(item: TireCheckpoint): TireCheckpointView {
    return TireCheckpointView(
        codeText = item.code,
        labelText = item.label,
        limitText = item.limit.toString(),
        timezone = truncate(item.timezone, 40),
        durationMinutesText = item.durationMinutes.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        archived = "${item.archived}",
        batchSize = item.batchSize.toString(),
        cost = "%.2f".format(item.cost),
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireCheckpointViews(items: MutableList<TireCheckpoint>): MutableList<TireCheckpointView> {
    val views = mutableListOf<TireCheckpointView>()
    for (item in items) {
        views.add(toTireCheckpointView(item))
    }
    return views
}

fun sumTireCheckpointView(view: TireCheckpointView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedLimit.toDouble() + parsedDurationMinutes.toDouble() + parsedLoadKg
}
