package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching adjustment. */
data class CoachingAdjustmentView(
    var code: String,
    var name: String,
    var amountText: String,
    var etaMinutesText: String,
    var palletsText: String,
    var threshold: String,
    var source: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toCoachingAdjustmentView(item: CoachingAdjustment): CoachingAdjustmentView {
    return CoachingAdjustmentView(
        code = item.code,
        name = item.name,
        amountText = "%.2f".format(item.amount),
        etaMinutesText = item.etaMinutes.toString(),
        palletsText = item.pallets.toString(),
        threshold = "%.2f".format(item.threshold),
        source = truncate(item.source, 40),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingAdjustmentViews(items: MutableList<CoachingAdjustment>): MutableList<CoachingAdjustmentView> {
    val views = mutableListOf<CoachingAdjustmentView>()
    for (item in items) {
        views.add(toCoachingAdjustmentView(item))
    }
    return views
}

fun sumCoachingAdjustmentView(view: CoachingAdjustmentView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedAmount + parsedEtaMinutes.toDouble() + parsedPallets.toDouble()
}
