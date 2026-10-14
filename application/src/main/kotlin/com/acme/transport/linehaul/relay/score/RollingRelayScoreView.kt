package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling relay score. */
data class RollingRelayScoreView(
    var code: String,
    var nameText: String,
    var trailerIdText: String,
    var label: String,
    var summary: String,
    var amountText: String,
    var statusLabel: String,
)

fun toRollingRelayScoreView(item: RollingRelayScore): RollingRelayScoreView {
    return RollingRelayScoreView(
        code = item.code,
        nameText = truncate(item.name, 80),
        trailerIdText = item.trailerId,
        label = item.label,
        summary = item.summary,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRelayScoreViews(items: MutableList<RollingRelayScore>): MutableList<RollingRelayScoreView> {
    val views = mutableListOf<RollingRelayScoreView>()
    for (item in items) {
        views.add(toRollingRelayScoreView(item))
    }
    return views
}

fun sumRollingRelayScoreView(view: RollingRelayScoreView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
