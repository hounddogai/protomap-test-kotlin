package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive capacity snapshot. */
data class AdaptiveCapacitySnapshotView(
    var code: String,
    var labelText: String,
    var locale: String,
    var currency: String,
    var floorValue: String,
    var approved: String,
    var archived: String,
    var statusLabel: String,
)

fun toAdaptiveCapacitySnapshotView(item: AdaptiveCapacitySnapshot): AdaptiveCapacitySnapshotView {
    return AdaptiveCapacitySnapshotView(
        code = item.code,
        labelText = item.label,
        locale = item.locale,
        currency = truncate(item.currency, 80),
        floorValue = "%.2f".format(item.floorValue),
        approved = "${item.approved}",
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCapacitySnapshotViews(items: MutableList<AdaptiveCapacitySnapshot>): MutableList<AdaptiveCapacitySnapshotView> {
    val views = mutableListOf<AdaptiveCapacitySnapshotView>()
    for (item in items) {
        views.add(toAdaptiveCapacitySnapshotView(item))
    }
    return views
}

fun sumAdaptiveCapacitySnapshotView(view: AdaptiveCapacitySnapshotView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFloorValue
}
