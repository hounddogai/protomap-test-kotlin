package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door record. */
data class DockDoorRecordView(
    var code: String,
    var title: String,
    var sequence: String,
    var countryText: String,
    var routeCodeText: String,
    var score: String,
    var distanceKm: String,
    var quantity: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toDockDoorRecordView(item: DockDoorRecord): DockDoorRecordView {
    return DockDoorRecordView(
        code = truncate(item.code, 64),
        title = item.title,
        sequence = item.sequence.toString(),
        countryText = item.country,
        routeCodeText = item.routeCode,
        score = "%.2f".format(item.score),
        distanceKm = "%.2f".format(item.distanceKm),
        quantity = item.quantity.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorRecordViews(items: MutableList<DockDoorRecord>): MutableList<DockDoorRecordView> {
    val views = mutableListOf<DockDoorRecordView>()
    for (item in items) {
        views.add(toDockDoorRecordView(item))
    }
    return views
}

fun sumDockDoorRecordView(view: DockDoorRecordView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedScore = view.score.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedSequence.toDouble() + parsedScore + parsedDistanceKm
}
