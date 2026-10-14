package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy ev charge override. */
data class LegacyEvChargeOverrideView(
    var code: String,
    var title: String,
    var discountText: String,
    var budgetText: String,
    var label: String,
    var stopCountText: String,
    var carrierText: String,
    var unit: String,
    var rate: String,
    var statusLabel: String,
)

fun toLegacyEvChargeOverrideView(item: LegacyEvChargeOverride): LegacyEvChargeOverrideView {
    return LegacyEvChargeOverrideView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 80),
        discountText = "%.2f".format(item.discount),
        budgetText = "%.2f".format(item.budget),
        label = item.label,
        stopCountText = item.stopCount.toString(),
        carrierText = truncate(item.carrier, 80),
        unit = item.unit,
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyEvChargeOverrideViews(items: MutableList<LegacyEvChargeOverride>): MutableList<LegacyEvChargeOverrideView> {
    val views = mutableListOf<LegacyEvChargeOverrideView>()
    for (item in items) {
        views.add(toLegacyEvChargeOverrideView(item))
    }
    return views
}

fun sumLegacyEvChargeOverrideView(view: LegacyEvChargeOverrideView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedDiscount + parsedBudget + parsedStopCount.toDouble()
}
