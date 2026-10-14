package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered attempt baseline. */
data class TieredAttemptBaselineView(
    var codeText: String,
    var labelText: String,
    var laneText: String,
    var carrierText: String,
    var title: String,
    var amount: String,
    var statusLabel: String,
)

fun toTieredAttemptBaselineView(item: TieredAttemptBaseline): TieredAttemptBaselineView {
    return TieredAttemptBaselineView(
        codeText = item.code,
        labelText = item.label,
        laneText = truncate(item.lane, 80),
        carrierText = item.carrier,
        title = item.title,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAttemptBaselineViews(items: MutableList<TieredAttemptBaseline>): MutableList<TieredAttemptBaselineView> {
    val views = mutableListOf<TieredAttemptBaselineView>()
    for (item in items) {
        views.add(toTieredAttemptBaselineView(item))
    }
    return views
}

fun sumTieredAttemptBaselineView(view: TieredAttemptBaselineView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
