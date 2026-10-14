package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door override. */
data class DockDoorOverrideView(
    var code: String,
    var labelText: String,
    var shiftCodeText: String,
    var driverId: String,
    var windowDays: String,
    var routeCodeText: String,
    var loadKg: String,
    var batchSizeText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toDockDoorOverrideView(item: DockDoorOverride): DockDoorOverrideView {
    return DockDoorOverrideView(
        code = item.code,
        labelText = item.label,
        shiftCodeText = truncate(item.shiftCode, 64),
        driverId = truncate(item.driverId, 64),
        windowDays = item.windowDays.toString(),
        routeCodeText = item.routeCode,
        loadKg = "%.2f".format(item.loadKg),
        batchSizeText = item.batchSize.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorOverrideViews(items: MutableList<DockDoorOverride>): MutableList<DockDoorOverrideView> {
    val views = mutableListOf<DockDoorOverrideView>()
    for (item in items) {
        views.add(toDockDoorOverrideView(item))
    }
    return views
}

fun sumDockDoorOverrideView(view: DockDoorOverrideView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedWindowDays.toDouble() + parsedLoadKg + parsedBatchSize.toDouble()
}
