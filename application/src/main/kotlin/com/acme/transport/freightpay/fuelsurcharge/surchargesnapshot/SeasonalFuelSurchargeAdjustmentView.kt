package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal fuel surcharge adjustment. */
data class SeasonalFuelSurchargeAdjustmentView(
    var code: String,
    var title: String,
    var attempts: String,
    var carrier: String,
    var amountText: String,
    var contactEmail: String,
    var ratio: String,
    var windowDays: String,
    var startsAt: String,
    var weight: String,
    var pallets: String,
    var statusLabel: String,
)

fun toSeasonalFuelSurchargeAdjustmentView(item: SeasonalFuelSurchargeAdjustment): SeasonalFuelSurchargeAdjustmentView {
    return SeasonalFuelSurchargeAdjustmentView(
        code = item.code,
        title = truncate(item.title, 120),
        attempts = item.attempts.toString(),
        carrier = item.carrier,
        amountText = "%.2f".format(item.amount),
        contactEmail = truncate(item.contactEmail, 64),
        ratio = "%.2f".format(item.ratio),
        windowDays = item.windowDays.toString(),
        startsAt = item.startsAt.toString(),
        weight = "%.2f".format(item.weight),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFuelSurchargeAdjustmentViews(items: MutableList<SeasonalFuelSurchargeAdjustment>): MutableList<SeasonalFuelSurchargeAdjustmentView> {
    val views = mutableListOf<SeasonalFuelSurchargeAdjustmentView>()
    for (item in items) {
        views.add(toSeasonalFuelSurchargeAdjustmentView(item))
    }
    return views
}

fun sumSeasonalFuelSurchargeAdjustmentView(view: SeasonalFuelSurchargeAdjustmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedAttempts.toDouble() + parsedAmount + parsedRatio
}
