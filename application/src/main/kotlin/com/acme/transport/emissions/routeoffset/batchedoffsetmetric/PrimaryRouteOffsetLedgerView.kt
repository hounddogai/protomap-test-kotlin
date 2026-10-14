package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary route offset ledger. */
data class PrimaryRouteOffsetLedgerView(
    var code: String,
    var label: String,
    var lane: String,
    var score: String,
    var startsAt: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toPrimaryRouteOffsetLedgerView(item: PrimaryRouteOffsetLedger): PrimaryRouteOffsetLedgerView {
    return PrimaryRouteOffsetLedgerView(
        code = truncate(item.code, 120),
        label = item.label,
        lane = item.lane,
        score = "%.2f".format(item.score),
        startsAt = item.startsAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRouteOffsetLedgerViews(items: MutableList<PrimaryRouteOffsetLedger>): MutableList<PrimaryRouteOffsetLedgerView> {
    val views = mutableListOf<PrimaryRouteOffsetLedgerView>()
    for (item in items) {
        views.add(toPrimaryRouteOffsetLedgerView(item))
    }
    return views
}

fun sumPrimaryRouteOffsetLedgerView(view: PrimaryRouteOffsetLedgerView): Double {
    val parsedScore = view.score.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedScore + parsedStartsAt.toDouble() + parsedThreshold
}
