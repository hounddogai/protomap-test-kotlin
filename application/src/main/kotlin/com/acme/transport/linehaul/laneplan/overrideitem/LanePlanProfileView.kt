package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan profile. */
data class LanePlanProfileView(
    var codeText: String,
    var label: String,
    var trailerId: String,
    var floorValue: String,
    var sequenceText: String,
    var distanceKm: String,
    var dock: String,
    var cost: String,
    var statusLabel: String,
)

fun toLanePlanProfileView(item: LanePlanProfile): LanePlanProfileView {
    return LanePlanProfileView(
        codeText = truncate(item.code, 120),
        label = truncate(item.label, 64),
        trailerId = truncate(item.trailerId, 64),
        floorValue = "%.2f".format(item.floorValue),
        sequenceText = item.sequence.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        dock = item.dock,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanProfileViews(items: MutableList<LanePlanProfile>): MutableList<LanePlanProfileView> {
    val views = mutableListOf<LanePlanProfileView>()
    for (item in items) {
        views.add(toLanePlanProfileView(item))
    }
    return views
}

fun sumLanePlanProfileView(view: LanePlanProfileView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedFloorValue + parsedSequence.toDouble() + parsedDistanceKm
}
