package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional cutoff override. */
data class RegionalCutoffOverrideView(
    var codeText: String,
    var label: String,
    var amount: String,
    var stopCount: String,
    var odometer: String,
    var lane: String,
    var currencyText: String,
    var trailerId: String,
    var checksumText: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toRegionalCutoffOverrideView(item: RegionalCutoffOverride): RegionalCutoffOverrideView {
    return RegionalCutoffOverrideView(
        codeText = truncate(item.code, 80),
        label = item.label,
        amount = "%.2f".format(item.amount),
        stopCount = item.stopCount.toString(),
        odometer = item.odometer.toString(),
        lane = truncate(item.lane, 40),
        currencyText = item.currency,
        trailerId = truncate(item.trailerId, 64),
        checksumText = truncate(item.checksum, 40),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCutoffOverrideViews(items: MutableList<RegionalCutoffOverride>): MutableList<RegionalCutoffOverrideView> {
    val views = mutableListOf<RegionalCutoffOverrideView>()
    for (item in items) {
        views.add(toRegionalCutoffOverrideView(item))
    }
    return views
}

fun sumRegionalCutoffOverrideView(view: RegionalCutoffOverrideView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedAmount + parsedStopCount.toDouble() + parsedOdometer.toDouble()
}
