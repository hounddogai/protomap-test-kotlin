package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan preference. */
data class LanePlanPreferenceView(
    var code: String,
    var label: String,
    var capacityText: String,
    var sequence: String,
    var floorValue: String,
    var ratio: String,
    var dockText: String,
    var externalRefText: String,
    var title: String,
    var depot: String,
    var statusLabel: String,
)

fun toLanePlanPreferenceView(item: LanePlanPreference): LanePlanPreferenceView {
    return LanePlanPreferenceView(
        code = item.code,
        label = item.label,
        capacityText = item.capacity.toString(),
        sequence = item.sequence.toString(),
        floorValue = "%.2f".format(item.floorValue),
        ratio = "%.2f".format(item.ratio),
        dockText = item.dock,
        externalRefText = item.externalRef,
        title = item.title,
        depot = truncate(item.depot, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanPreferenceViews(items: MutableList<LanePlanPreference>): MutableList<LanePlanPreferenceView> {
    val views = mutableListOf<LanePlanPreferenceView>()
    for (item in items) {
        views.add(toLanePlanPreferenceView(item))
    }
    return views
}

fun sumLanePlanPreferenceView(view: LanePlanPreferenceView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedCapacity.toDouble() + parsedSequence.toDouble() + parsedFloorValue
}
