package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted scorecard notice. */
data class WeightedScorecardNoticeView(
    var code: String,
    var label: String,
    var vehicleIdText: String,
    var dock: String,
    var lane: String,
    var threshold: String,
    var etaMinutes: String,
    var createdAt: String,
    var unitText: String,
    var statusLabel: String,
)

fun toWeightedScorecardNoticeView(item: WeightedScorecardNotice): WeightedScorecardNoticeView {
    return WeightedScorecardNoticeView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 120),
        vehicleIdText = item.vehicleId,
        dock = item.dock,
        lane = item.lane,
        threshold = "%.2f".format(item.threshold),
        etaMinutes = item.etaMinutes.toString(),
        createdAt = item.createdAt.toString(),
        unitText = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedScorecardNoticeViews(items: MutableList<WeightedScorecardNotice>): MutableList<WeightedScorecardNoticeView> {
    val views = mutableListOf<WeightedScorecardNoticeView>()
    for (item in items) {
        views.add(toWeightedScorecardNoticeView(item))
    }
    return views
}

fun sumWeightedScorecardNoticeView(view: WeightedScorecardNoticeView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedThreshold + parsedEtaMinutes.toDouble() + parsedCreatedAt.toDouble()
}
