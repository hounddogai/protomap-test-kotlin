package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell baseline. */
data class DwellBaselineView(
    var codeText: String,
    var title: String,
    var floorValue: String,
    var active: String,
    var rate: String,
    var minimum: String,
    var sequenceText: String,
    var depot: String,
    var revision: String,
    var region: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toDwellBaselineView(item: DwellBaseline): DwellBaselineView {
    return DwellBaselineView(
        codeText = item.code,
        title = truncate(item.title, 120),
        floorValue = "%.2f".format(item.floorValue),
        active = "${item.active}",
        rate = "%.2f".format(item.rate),
        minimum = "%.2f".format(item.minimum),
        sequenceText = item.sequence.toString(),
        depot = truncate(item.depot, 120),
        revision = item.revision.toString(),
        region = truncate(item.region, 64),
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellBaselineViews(items: MutableList<DwellBaseline>): MutableList<DwellBaselineView> {
    val views = mutableListOf<DwellBaselineView>()
    for (item in items) {
        views.add(toDwellBaselineView(item))
    }
    return views
}

fun sumDwellBaselineView(view: DwellBaselineView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedFloorValue + parsedRate + parsedMinimum
}
