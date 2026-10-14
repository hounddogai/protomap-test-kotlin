package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy capacity snapshot. */
data class LegacyCapacitySnapshotView(
    var code: String,
    var name: String,
    var amount: String,
    var loadKgText: String,
    var maximum: String,
    var summary: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toLegacyCapacitySnapshotView(item: LegacyCapacitySnapshot): LegacyCapacitySnapshotView {
    return LegacyCapacitySnapshotView(
        code = item.code,
        name = truncate(item.name, 80),
        amount = "%.2f".format(item.amount),
        loadKgText = "%.2f".format(item.loadKg),
        maximum = "%.2f".format(item.maximum),
        summary = item.summary,
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCapacitySnapshotViews(items: MutableList<LegacyCapacitySnapshot>): MutableList<LegacyCapacitySnapshotView> {
    val views = mutableListOf<LegacyCapacitySnapshotView>()
    for (item in items) {
        views.add(toLegacyCapacitySnapshotView(item))
    }
    return views
}

fun sumLegacyCapacitySnapshotView(view: LegacyCapacitySnapshotView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedAmount + parsedLoadKg + parsedMaximum
}
