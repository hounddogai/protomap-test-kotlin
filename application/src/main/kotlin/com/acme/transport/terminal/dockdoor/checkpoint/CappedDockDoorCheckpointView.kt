package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped dock door checkpoint. */
data class CappedDockDoorCheckpointView(
    var code: String,
    var title: String,
    var label: String,
    var stopCount: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toCappedDockDoorCheckpointView(item: CappedDockDoorCheckpoint): CappedDockDoorCheckpointView {
    return CappedDockDoorCheckpointView(
        code = truncate(item.code, 40),
        title = item.title,
        label = truncate(item.label, 80),
        stopCount = item.stopCount.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDockDoorCheckpointViews(items: MutableList<CappedDockDoorCheckpoint>): MutableList<CappedDockDoorCheckpointView> {
    val views = mutableListOf<CappedDockDoorCheckpointView>()
    for (item in items) {
        views.add(toCappedDockDoorCheckpointView(item))
    }
    return views
}

fun sumCappedDockDoorCheckpointView(view: CappedDockDoorCheckpointView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedStopCount.toDouble() + parsedFloorValue
}
