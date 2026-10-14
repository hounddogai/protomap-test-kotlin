package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tender snapshot. */
data class ManualTenderSnapshotView(
    var code: String,
    var label: String,
    var palletsText: String,
    var active: String,
    var priority: String,
    var titleText: String,
    var odometerText: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toManualTenderSnapshotView(item: ManualTenderSnapshot): ManualTenderSnapshotView {
    return ManualTenderSnapshotView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 40),
        palletsText = item.pallets.toString(),
        active = "${item.active}",
        priority = item.priority.toString(),
        titleText = item.title,
        odometerText = item.odometer.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTenderSnapshotViews(items: MutableList<ManualTenderSnapshot>): MutableList<ManualTenderSnapshotView> {
    val views = mutableListOf<ManualTenderSnapshotView>()
    for (item in items) {
        views.add(toManualTenderSnapshotView(item))
    }
    return views
}

fun sumManualTenderSnapshotView(view: ManualTenderSnapshotView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedPallets.toDouble() + parsedPriority.toDouble() + parsedOdometer.toDouble()
}
