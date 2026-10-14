package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy qualification metric. */
data class LegacyQualificationMetricView(
    var codeText: String,
    var label: String,
    var notesText: String,
    var score: String,
    var updatedAt: String,
    var pallets: String,
    var driverId: String,
    var externalRef: String,
    var amount: String,
    var attempts: String,
    var statusLabel: String,
)

fun toLegacyQualificationMetricView(item: LegacyQualificationMetric): LegacyQualificationMetricView {
    return LegacyQualificationMetricView(
        codeText = item.code,
        label = item.label,
        notesText = item.notes,
        score = "%.2f".format(item.score),
        updatedAt = item.updatedAt.toString(),
        pallets = item.pallets.toString(),
        driverId = truncate(item.driverId, 40),
        externalRef = item.externalRef,
        amount = "%.2f".format(item.amount),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyQualificationMetricViews(items: MutableList<LegacyQualificationMetric>): MutableList<LegacyQualificationMetricView> {
    val views = mutableListOf<LegacyQualificationMetricView>()
    for (item in items) {
        views.add(toLegacyQualificationMetricView(item))
    }
    return views
}

fun sumLegacyQualificationMetricView(view: LegacyQualificationMetricView): Double {
    val parsedScore = view.score.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedScore + parsedUpdatedAt.toDouble() + parsedPallets.toDouble()
}
