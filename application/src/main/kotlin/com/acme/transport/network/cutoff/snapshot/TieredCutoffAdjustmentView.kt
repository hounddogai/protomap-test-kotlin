package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered cutoff adjustment. */
data class TieredCutoffAdjustmentView(
    var codeText: String,
    var labelText: String,
    var discount: String,
    var titleText: String,
    var timezone: String,
    var amount: String,
    var trailerId: String,
    var countryText: String,
    var retriesText: String,
    var currency: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toTieredCutoffAdjustmentView(item: TieredCutoffAdjustment): TieredCutoffAdjustmentView {
    return TieredCutoffAdjustmentView(
        codeText = item.code,
        labelText = item.label,
        discount = "%.2f".format(item.discount),
        titleText = item.title,
        timezone = item.timezone,
        amount = "%.2f".format(item.amount),
        trailerId = truncate(item.trailerId, 120),
        countryText = item.country,
        retriesText = item.retries.toString(),
        currency = truncate(item.currency, 40),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCutoffAdjustmentViews(items: MutableList<TieredCutoffAdjustment>): MutableList<TieredCutoffAdjustmentView> {
    val views = mutableListOf<TieredCutoffAdjustmentView>()
    for (item in items) {
        views.add(toTieredCutoffAdjustmentView(item))
    }
    return views
}

fun sumTieredCutoffAdjustmentView(view: TieredCutoffAdjustmentView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedDiscount + parsedAmount + parsedRetries.toDouble()
}
