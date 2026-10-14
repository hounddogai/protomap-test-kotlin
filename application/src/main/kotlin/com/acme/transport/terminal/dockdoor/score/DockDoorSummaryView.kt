package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door summary. */
data class DockDoorSummaryView(
    var codeText: String,
    var name: String,
    var vehicleIdText: String,
    var distanceKm: String,
    var sourceText: String,
    var score: String,
    var graceDays: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toDockDoorSummaryView(item: DockDoorSummary): DockDoorSummaryView {
    return DockDoorSummaryView(
        codeText = truncate(item.code, 120),
        name = item.name,
        vehicleIdText = item.vehicleId,
        distanceKm = "%.2f".format(item.distanceKm),
        sourceText = item.source,
        score = "%.2f".format(item.score),
        graceDays = item.graceDays.toString(),
        carrierText = truncate(item.carrier, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorSummaryViews(items: MutableList<DockDoorSummary>): MutableList<DockDoorSummaryView> {
    val views = mutableListOf<DockDoorSummaryView>()
    for (item in items) {
        views.add(toDockDoorSummaryView(item))
    }
    return views
}

fun sumDockDoorSummaryView(view: DockDoorSummaryView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedDistanceKm + parsedScore + parsedGraceDays.toDouble()
}
