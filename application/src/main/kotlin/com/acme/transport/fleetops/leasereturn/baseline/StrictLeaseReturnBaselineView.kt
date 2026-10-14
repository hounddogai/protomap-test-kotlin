package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict lease return baseline. */
data class StrictLeaseReturnBaselineView(
    var code: String,
    var labelText: String,
    var category: String,
    var segmentText: String,
    var minimum: String,
    var palletsText: String,
    var maximumText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toStrictLeaseReturnBaselineView(item: StrictLeaseReturnBaseline): StrictLeaseReturnBaselineView {
    return StrictLeaseReturnBaselineView(
        code = truncate(item.code, 40),
        labelText = truncate(item.label, 80),
        category = item.category,
        segmentText = item.segment,
        minimum = "%.2f".format(item.minimum),
        palletsText = item.pallets.toString(),
        maximumText = "%.2f".format(item.maximum),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictLeaseReturnBaselineViews(items: MutableList<StrictLeaseReturnBaseline>): MutableList<StrictLeaseReturnBaselineView> {
    val views = mutableListOf<StrictLeaseReturnBaselineView>()
    for (item in items) {
        views.add(toStrictLeaseReturnBaselineView(item))
    }
    return views
}

fun sumStrictLeaseReturnBaselineView(view: StrictLeaseReturnBaselineView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMinimum + parsedPallets.toDouble() + parsedMaximum
}
