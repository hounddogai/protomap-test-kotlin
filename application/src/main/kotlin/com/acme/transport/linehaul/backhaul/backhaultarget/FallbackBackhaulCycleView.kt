package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback backhaul cycle. */
data class FallbackBackhaulCycleView(
    var code: String,
    var titleText: String,
    var startsAt: String,
    var floorValue: String,
    var loadKgText: String,
    var capacityText: String,
    var expiresAt: String,
    var summary: String,
    var checksum: String,
    var driverId: String,
    var statusLabel: String,
)

fun toFallbackBackhaulCycleView(item: FallbackBackhaulCycle): FallbackBackhaulCycleView {
    return FallbackBackhaulCycleView(
        code = truncate(item.code, 120),
        titleText = truncate(item.title, 40),
        startsAt = item.startsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        loadKgText = "%.2f".format(item.loadKg),
        capacityText = item.capacity.toString(),
        expiresAt = item.expiresAt.toString(),
        summary = truncate(item.summary, 64),
        checksum = item.checksum,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackBackhaulCycleViews(items: MutableList<FallbackBackhaulCycle>): MutableList<FallbackBackhaulCycleView> {
    val views = mutableListOf<FallbackBackhaulCycleView>()
    for (item in items) {
        views.add(toFallbackBackhaulCycleView(item))
    }
    return views
}

fun sumFallbackBackhaulCycleView(view: FallbackBackhaulCycleView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedStartsAt.toDouble() + parsedFloorValue + parsedLoadKg
}
