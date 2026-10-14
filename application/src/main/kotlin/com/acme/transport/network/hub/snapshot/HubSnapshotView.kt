package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub snapshot. */
data class HubSnapshotView(
    var code: String,
    var titleText: String,
    var durationMinutes: String,
    var ceilingText: String,
    var lane: String,
    var minimum: String,
    var ratio: String,
    var statusLabel: String,
)

fun toHubSnapshotView(item: HubSnapshot): HubSnapshotView {
    return HubSnapshotView(
        code = item.code,
        titleText = truncate(item.title, 64),
        durationMinutes = item.durationMinutes.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        lane = truncate(item.lane, 120),
        minimum = "%.2f".format(item.minimum),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubSnapshotViews(items: MutableList<HubSnapshot>): MutableList<HubSnapshotView> {
    val views = mutableListOf<HubSnapshotView>()
    for (item in items) {
        views.add(toHubSnapshotView(item))
    }
    return views
}

fun sumHubSnapshotView(view: HubSnapshotView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedDurationMinutes.toDouble() + parsedCeiling + parsedMinimum
}
