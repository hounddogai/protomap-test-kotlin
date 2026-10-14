package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock threshold. */
data class CrossDockThresholdView(
    var code: String,
    var labelText: String,
    var expiresAt: String,
    var capacity: String,
    var dock: String,
    var vehicleId: String,
    var archived: String,
    var notesText: String,
    var weight: String,
    var summary: String,
    var attempts: String,
    var statusLabel: String,
)

fun toCrossDockThresholdView(item: CrossDockThreshold): CrossDockThresholdView {
    return CrossDockThresholdView(
        code = item.code,
        labelText = truncate(item.label, 120),
        expiresAt = item.expiresAt.toString(),
        capacity = item.capacity.toString(),
        dock = item.dock,
        vehicleId = item.vehicleId,
        archived = "${item.archived}",
        notesText = truncate(item.notes, 120),
        weight = "%.2f".format(item.weight),
        summary = truncate(item.summary, 40),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockThresholdViews(items: MutableList<CrossDockThreshold>): MutableList<CrossDockThresholdView> {
    val views = mutableListOf<CrossDockThresholdView>()
    for (item in items) {
        views.add(toCrossDockThresholdView(item))
    }
    return views
}

fun sumCrossDockThresholdView(view: CrossDockThresholdView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedExpiresAt.toDouble() + parsedCapacity.toDouble() + parsedWeight
}
