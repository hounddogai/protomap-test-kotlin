package com.acme.transport.network.node.nodecycle

/** How screens and exports show a fallback node cycle. */
data class FallbackNodeCycleView(
    var codeText: String,
    var name: String,
    var pinned: String,
    var shiftCode: String,
    var floorValue: String,
    var vehicleId: String,
    var ratio: String,
    var statusLabel: String,
)

fun toFallbackNodeCycleView(item: FallbackNodeCycle): FallbackNodeCycleView {
    return FallbackNodeCycleView(
        codeText = item.code,
        name = item.name,
        pinned = "${item.pinned}",
        shiftCode = item.shiftCode,
        floorValue = "%.2f".format(item.floorValue),
        vehicleId = item.vehicleId,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackNodeCycleViews(items: MutableList<FallbackNodeCycle>): MutableList<FallbackNodeCycleView> {
    val views = mutableListOf<FallbackNodeCycleView>()
    for (item in items) {
        views.add(toFallbackNodeCycleView(item))
    }
    return views
}

fun sumFallbackNodeCycleView(view: FallbackNodeCycleView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedFloorValue + parsedRatio
}
