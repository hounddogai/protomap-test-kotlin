package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock digest. */
data class CrossDockDigestView(
    var codeText: String,
    var title: String,
    var rate: String,
    var floorValue: String,
    var trailerId: String,
    var dock: String,
    var revision: String,
    var pinned: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toCrossDockDigestView(item: CrossDockDigest): CrossDockDigestView {
    return CrossDockDigestView(
        codeText = item.code,
        title = truncate(item.title, 40),
        rate = "%.2f".format(item.rate),
        floorValue = "%.2f".format(item.floorValue),
        trailerId = truncate(item.trailerId, 64),
        dock = item.dock,
        revision = item.revision.toString(),
        pinned = "${item.pinned}",
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockDigestViews(items: MutableList<CrossDockDigest>): MutableList<CrossDockDigestView> {
    val views = mutableListOf<CrossDockDigestView>()
    for (item in items) {
        views.add(toCrossDockDigestView(item))
    }
    return views
}

fun sumCrossDockDigestView(view: CrossDockDigestView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedRate + parsedFloorValue + parsedRevision.toDouble()
}
