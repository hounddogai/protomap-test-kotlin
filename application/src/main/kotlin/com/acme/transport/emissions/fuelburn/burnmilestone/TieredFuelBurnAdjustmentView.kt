package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered fuel burn adjustment. */
data class TieredFuelBurnAdjustmentView(
    var code: String,
    var label: String,
    var attempts: String,
    var updatedAtText: String,
    var pinned: String,
    var floorValue: String,
    var fuelLiters: String,
    var contactEmail: String,
    var ratio: String,
    var phone: String,
    var statusLabel: String,
)

fun toTieredFuelBurnAdjustmentView(item: TieredFuelBurnAdjustment): TieredFuelBurnAdjustmentView {
    return TieredFuelBurnAdjustmentView(
        code = item.code,
        label = item.label,
        attempts = item.attempts.toString(),
        updatedAtText = item.updatedAt.toString(),
        pinned = "${item.pinned}",
        floorValue = "%.2f".format(item.floorValue),
        fuelLiters = "%.2f".format(item.fuelLiters),
        contactEmail = item.contactEmail,
        ratio = "%.2f".format(item.ratio),
        phone = truncate(item.phone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFuelBurnAdjustmentViews(items: MutableList<TieredFuelBurnAdjustment>): MutableList<TieredFuelBurnAdjustmentView> {
    val views = mutableListOf<TieredFuelBurnAdjustmentView>()
    for (item in items) {
        views.add(toTieredFuelBurnAdjustmentView(item))
    }
    return views
}

fun sumTieredFuelBurnAdjustmentView(view: TieredFuelBurnAdjustmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedAttempts.toDouble() + parsedUpdatedAt.toDouble() + parsedFloorValue
}
