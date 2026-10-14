package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary dwell snapshot. */
data class PrimaryDwellSnapshotView(
    var code: String,
    var label: String,
    var maximum: String,
    var stopCount: String,
    var startsAt: String,
    var visible: String,
    var graceDaysText: String,
    var channel: String,
    var dock: String,
    var statusLabel: String,
)

fun toPrimaryDwellSnapshotView(item: PrimaryDwellSnapshot): PrimaryDwellSnapshotView {
    return PrimaryDwellSnapshotView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 64),
        maximum = "%.2f".format(item.maximum),
        stopCount = item.stopCount.toString(),
        startsAt = item.startsAt.toString(),
        visible = "${item.visible}",
        graceDaysText = item.graceDays.toString(),
        channel = item.channel,
        dock = truncate(item.dock, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDwellSnapshotViews(items: MutableList<PrimaryDwellSnapshot>): MutableList<PrimaryDwellSnapshotView> {
    val views = mutableListOf<PrimaryDwellSnapshotView>()
    for (item in items) {
        views.add(toPrimaryDwellSnapshotView(item))
    }
    return views
}

fun sumPrimaryDwellSnapshotView(view: PrimaryDwellSnapshotView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMaximum + parsedStopCount.toDouble() + parsedStartsAt.toDouble()
}
