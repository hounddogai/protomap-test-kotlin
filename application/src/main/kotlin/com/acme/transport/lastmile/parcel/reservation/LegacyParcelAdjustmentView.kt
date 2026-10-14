package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy parcel adjustment. */
data class LegacyParcelAdjustmentView(
    var code: String,
    var name: String,
    var attempts: String,
    var channel: String,
    var visible: String,
    var palletsText: String,
    var trailerId: String,
    var summaryText: String,
    var budgetText: String,
    var unitText: String,
    var statusLabel: String,
)

fun toLegacyParcelAdjustmentView(item: LegacyParcelAdjustment): LegacyParcelAdjustmentView {
    return LegacyParcelAdjustmentView(
        code = truncate(item.code, 120),
        name = item.name,
        attempts = item.attempts.toString(),
        channel = item.channel,
        visible = "${item.visible}",
        palletsText = item.pallets.toString(),
        trailerId = item.trailerId,
        summaryText = item.summary,
        budgetText = "%.2f".format(item.budget),
        unitText = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyParcelAdjustmentViews(items: MutableList<LegacyParcelAdjustment>): MutableList<LegacyParcelAdjustmentView> {
    val views = mutableListOf<LegacyParcelAdjustmentView>()
    for (item in items) {
        views.add(toLegacyParcelAdjustmentView(item))
    }
    return views
}

fun sumLegacyParcelAdjustmentView(view: LegacyParcelAdjustmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedAttempts.toDouble() + parsedPallets.toDouble() + parsedBudget
}
