package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional defect snapshot. */
data class RegionalDefectSnapshotView(
    var code: String,
    var label: String,
    var endsAtText: String,
    var createdAt: String,
    var driverId: String,
    var graceDays: String,
    var regionText: String,
    var windowDays: String,
    var quantityText: String,
    var attempts: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toRegionalDefectSnapshotView(item: RegionalDefectSnapshot): RegionalDefectSnapshotView {
    return RegionalDefectSnapshotView(
        code = truncate(item.code, 120),
        label = item.label,
        endsAtText = item.endsAt.toString(),
        createdAt = item.createdAt.toString(),
        driverId = truncate(item.driverId, 64),
        graceDays = item.graceDays.toString(),
        regionText = item.region,
        windowDays = item.windowDays.toString(),
        quantityText = item.quantity.toString(),
        attempts = item.attempts.toString(),
        carrierText = truncate(item.carrier, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDefectSnapshotViews(items: MutableList<RegionalDefectSnapshot>): MutableList<RegionalDefectSnapshotView> {
    val views = mutableListOf<RegionalDefectSnapshotView>()
    for (item in items) {
        views.add(toRegionalDefectSnapshotView(item))
    }
    return views
}

fun sumRegionalDefectSnapshotView(view: RegionalDefectSnapshotView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedEndsAt.toDouble() + parsedCreatedAt.toDouble() + parsedGraceDays.toDouble()
}
