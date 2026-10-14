package com.acme.transport.network.zone.agreement

/** How screens and exports show a regional zone checkpoint. */
data class RegionalZoneCheckpointView(
    var codeText: String,
    var title: String,
    var threshold: String,
    var windowDays: String,
    var minimum: String,
    var statusLabel: String,
)

fun toRegionalZoneCheckpointView(item: RegionalZoneCheckpoint): RegionalZoneCheckpointView {
    return RegionalZoneCheckpointView(
        codeText = item.code,
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        windowDays = item.windowDays.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalZoneCheckpointViews(items: MutableList<RegionalZoneCheckpoint>): MutableList<RegionalZoneCheckpointView> {
    val views = mutableListOf<RegionalZoneCheckpointView>()
    for (item in items) {
        views.add(toRegionalZoneCheckpointView(item))
    }
    return views
}

fun sumRegionalZoneCheckpointView(view: RegionalZoneCheckpointView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedThreshold + parsedWindowDays.toDouble() + parsedMinimum
}
