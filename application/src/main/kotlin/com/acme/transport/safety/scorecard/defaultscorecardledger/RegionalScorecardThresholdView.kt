package com.acme.transport.safety.scorecard.defaultscorecardledger

/** How screens and exports show a regional scorecard threshold. */
data class RegionalScorecardThresholdView(
    var codeText: String,
    var nameText: String,
    var sequence: String,
    var notes: String,
    var etaMinutes: String,
    var weightText: String,
    var attempts: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toRegionalScorecardThresholdView(item: RegionalScorecardThreshold): RegionalScorecardThresholdView {
    return RegionalScorecardThresholdView(
        codeText = item.code,
        nameText = item.name,
        sequence = item.sequence.toString(),
        notes = item.notes,
        etaMinutes = item.etaMinutes.toString(),
        weightText = "%.2f".format(item.weight),
        attempts = item.attempts.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalScorecardThresholdViews(items: MutableList<RegionalScorecardThreshold>): MutableList<RegionalScorecardThresholdView> {
    val views = mutableListOf<RegionalScorecardThresholdView>()
    for (item in items) {
        views.add(toRegionalScorecardThresholdView(item))
    }
    return views
}

fun sumRegionalScorecardThresholdView(view: RegionalScorecardThresholdView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedSequence.toDouble() + parsedEtaMinutes.toDouble() + parsedWeight
}
