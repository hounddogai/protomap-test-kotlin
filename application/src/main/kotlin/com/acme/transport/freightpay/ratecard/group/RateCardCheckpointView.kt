package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card checkpoint. */
data class RateCardCheckpointView(
    var code: String,
    var label: String,
    var currency: String,
    var driverId: String,
    var timezone: String,
    var createdAt: String,
    var pallets: String,
    var discount: String,
    var dockText: String,
    var statusLabel: String,
)

fun toRateCardCheckpointView(item: RateCardCheckpoint): RateCardCheckpointView {
    return RateCardCheckpointView(
        code = item.code,
        label = item.label,
        currency = item.currency,
        driverId = truncate(item.driverId, 64),
        timezone = item.timezone,
        createdAt = item.createdAt.toString(),
        pallets = item.pallets.toString(),
        discount = "%.2f".format(item.discount),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardCheckpointViews(items: MutableList<RateCardCheckpoint>): MutableList<RateCardCheckpointView> {
    val views = mutableListOf<RateCardCheckpointView>()
    for (item in items) {
        views.add(toRateCardCheckpointView(item))
    }
    return views
}

fun sumRateCardCheckpointView(view: RateCardCheckpointView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedCreatedAt.toDouble() + parsedPallets.toDouble() + parsedDiscount
}
