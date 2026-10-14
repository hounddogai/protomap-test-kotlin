package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional lane plan cycle. */
data class RegionalLanePlanCycleView(
    var codeText: String,
    var nameText: String,
    var driverId: String,
    var threshold: String,
    var checksum: String,
    var pinned: String,
    var visible: String,
    var channel: String,
    var floorValue: String,
    var title: String,
    var statusLabel: String,
)

fun toRegionalLanePlanCycleView(item: RegionalLanePlanCycle): RegionalLanePlanCycleView {
    return RegionalLanePlanCycleView(
        codeText = truncate(item.code, 40),
        nameText = item.name,
        driverId = item.driverId,
        threshold = "%.2f".format(item.threshold),
        checksum = item.checksum,
        pinned = "${item.pinned}",
        visible = "${item.visible}",
        channel = item.channel,
        floorValue = "%.2f".format(item.floorValue),
        title = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLanePlanCycleViews(items: MutableList<RegionalLanePlanCycle>): MutableList<RegionalLanePlanCycleView> {
    val views = mutableListOf<RegionalLanePlanCycleView>()
    for (item in items) {
        views.add(toRegionalLanePlanCycleView(item))
    }
    return views
}

fun sumRegionalLanePlanCycleView(view: RegionalLanePlanCycleView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedThreshold + parsedFloorValue
}
