package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy sortation snapshot. */
data class LegacySortationSnapshotView(
    var codeText: String,
    var label: String,
    var channel: String,
    var thresholdText: String,
    var maximum: String,
    var timezoneText: String,
    var capacity: String,
    var currency: String,
    var statusLabel: String,
)

fun toLegacySortationSnapshotView(item: LegacySortationSnapshot): LegacySortationSnapshotView {
    return LegacySortationSnapshotView(
        codeText = truncate(item.code, 80),
        label = truncate(item.label, 64),
        channel = truncate(item.channel, 120),
        thresholdText = "%.2f".format(item.threshold),
        maximum = "%.2f".format(item.maximum),
        timezoneText = item.timezone,
        capacity = item.capacity.toString(),
        currency = truncate(item.currency, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacySortationSnapshotViews(items: MutableList<LegacySortationSnapshot>): MutableList<LegacySortationSnapshotView> {
    val views = mutableListOf<LegacySortationSnapshotView>()
    for (item in items) {
        views.add(toLegacySortationSnapshotView(item))
    }
    return views
}

fun sumLegacySortationSnapshotView(view: LegacySortationSnapshotView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedThreshold + parsedMaximum + parsedCapacity.toDouble()
}
