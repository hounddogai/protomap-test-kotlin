package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global inspection threshold. */
data class GlobalInspectionThresholdView(
    var code: String,
    var title: String,
    var laneText: String,
    var depot: String,
    var region: String,
    var dockText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toGlobalInspectionThresholdView(item: GlobalInspectionThreshold): GlobalInspectionThresholdView {
    return GlobalInspectionThresholdView(
        code = item.code,
        title = item.title,
        laneText = truncate(item.lane, 120),
        depot = truncate(item.depot, 40),
        region = item.region,
        dockText = item.dock,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionThresholdViews(items: MutableList<GlobalInspectionThreshold>): MutableList<GlobalInspectionThresholdView> {
    val views = mutableListOf<GlobalInspectionThresholdView>()
    for (item in items) {
        views.add(toGlobalInspectionThresholdView(item))
    }
    return views
}

fun sumGlobalInspectionThresholdView(view: GlobalInspectionThresholdView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
