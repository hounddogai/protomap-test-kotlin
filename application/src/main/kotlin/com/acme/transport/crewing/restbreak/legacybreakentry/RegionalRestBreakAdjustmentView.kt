package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional rest break adjustment. */
data class RegionalRestBreakAdjustmentView(
    var codeText: String,
    var nameText: String,
    var descriptionText: String,
    var vehicleIdText: String,
    var etaMinutes: String,
    var expiresAt: String,
    var floorValue: String,
    var contactEmail: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toRegionalRestBreakAdjustmentView(item: RegionalRestBreakAdjustment): RegionalRestBreakAdjustmentView {
    return RegionalRestBreakAdjustmentView(
        codeText = truncate(item.code, 80),
        nameText = item.name,
        descriptionText = truncate(item.description, 40),
        vehicleIdText = item.vehicleId,
        etaMinutes = item.etaMinutes.toString(),
        expiresAt = item.expiresAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        contactEmail = truncate(item.contactEmail, 64),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRestBreakAdjustmentViews(items: MutableList<RegionalRestBreakAdjustment>): MutableList<RegionalRestBreakAdjustmentView> {
    val views = mutableListOf<RegionalRestBreakAdjustmentView>()
    for (item in items) {
        views.add(toRegionalRestBreakAdjustmentView(item))
    }
    return views
}

fun sumRegionalRestBreakAdjustmentView(view: RegionalRestBreakAdjustmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble() + parsedFloorValue
}
