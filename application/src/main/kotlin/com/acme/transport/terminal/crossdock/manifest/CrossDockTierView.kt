package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock tier. */
data class CrossDockTierView(
    var codeText: String,
    var name: String,
    var driverId: String,
    var createdAt: String,
    var source: String,
    var minimumText: String,
    var floorValue: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toCrossDockTierView(item: CrossDockTier): CrossDockTierView {
    return CrossDockTierView(
        codeText = item.code,
        name = item.name,
        driverId = truncate(item.driverId, 40),
        createdAt = item.createdAt.toString(),
        source = item.source,
        minimumText = "%.2f".format(item.minimum),
        floorValue = "%.2f".format(item.floorValue),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockTierViews(items: MutableList<CrossDockTier>): MutableList<CrossDockTierView> {
    val views = mutableListOf<CrossDockTierView>()
    for (item in items) {
        views.add(toCrossDockTierView(item))
    }
    return views
}

fun sumCrossDockTierView(view: CrossDockTierView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedCreatedAt.toDouble() + parsedMinimum + parsedFloorValue
}
