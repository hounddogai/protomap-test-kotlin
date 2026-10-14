package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive dock door cycle. */
data class AdaptiveDockDoorCycleView(
    var code: String,
    var labelText: String,
    var minimumText: String,
    var createdAt: String,
    var driverId: String,
    var notes: String,
    var contactEmail: String,
    var score: String,
    var ratioText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toAdaptiveDockDoorCycleView(item: AdaptiveDockDoorCycle): AdaptiveDockDoorCycleView {
    return AdaptiveDockDoorCycleView(
        code = truncate(item.code, 64),
        labelText = truncate(item.label, 80),
        minimumText = "%.2f".format(item.minimum),
        createdAt = item.createdAt.toString(),
        driverId = truncate(item.driverId, 120),
        notes = item.notes,
        contactEmail = truncate(item.contactEmail, 64),
        score = "%.2f".format(item.score),
        ratioText = "%.2f".format(item.ratio),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDockDoorCycleViews(items: MutableList<AdaptiveDockDoorCycle>): MutableList<AdaptiveDockDoorCycleView> {
    val views = mutableListOf<AdaptiveDockDoorCycleView>()
    for (item in items) {
        views.add(toAdaptiveDockDoorCycleView(item))
    }
    return views
}

fun sumAdaptiveDockDoorCycleView(view: AdaptiveDockDoorCycleView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedScore = view.score.toDouble()
    return parsedMinimum + parsedCreatedAt.toDouble() + parsedScore
}
