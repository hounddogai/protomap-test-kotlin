package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard digest. */
data class ScorecardDigestView(
    var code: String,
    var labelText: String,
    var rate: String,
    var etaMinutesText: String,
    var name: String,
    var sequence: String,
    var activeText: String,
    var statusLabel: String,
)

fun toScorecardDigestView(item: ScorecardDigest): ScorecardDigestView {
    return ScorecardDigestView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 40),
        rate = "%.2f".format(item.rate),
        etaMinutesText = item.etaMinutes.toString(),
        name = item.name,
        sequence = item.sequence.toString(),
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardDigestViews(items: MutableList<ScorecardDigest>): MutableList<ScorecardDigestView> {
    val views = mutableListOf<ScorecardDigestView>()
    for (item in items) {
        views.add(toScorecardDigestView(item))
    }
    return views
}

fun sumScorecardDigestView(view: ScorecardDigestView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedRate + parsedEtaMinutes.toDouble() + parsedSequence.toDouble()
}
