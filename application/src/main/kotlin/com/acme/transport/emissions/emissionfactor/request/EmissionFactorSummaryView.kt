package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor summary. */
data class EmissionFactorSummaryView(
    var code: String,
    var title: String,
    var attemptsText: String,
    var reviewer: String,
    var region: String,
    var sequence: String,
    var ceiling: String,
    var carrier: String,
    var summary: String,
    var contactEmailText: String,
    var source: String,
    var statusLabel: String,
)

fun toEmissionFactorSummaryView(item: EmissionFactorSummary): EmissionFactorSummaryView {
    return EmissionFactorSummaryView(
        code = truncate(item.code, 80),
        title = item.title,
        attemptsText = item.attempts.toString(),
        reviewer = item.reviewer,
        region = item.region,
        sequence = item.sequence.toString(),
        ceiling = "%.2f".format(item.ceiling),
        carrier = item.carrier,
        summary = item.summary,
        contactEmailText = item.contactEmail,
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorSummaryViews(items: MutableList<EmissionFactorSummary>): MutableList<EmissionFactorSummaryView> {
    val views = mutableListOf<EmissionFactorSummaryView>()
    for (item in items) {
        views.add(toEmissionFactorSummaryView(item))
    }
    return views
}

fun sumEmissionFactorSummaryView(view: EmissionFactorSummaryView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedAttempts.toDouble() + parsedSequence.toDouble() + parsedCeiling
}
