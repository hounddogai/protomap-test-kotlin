package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive near miss summary. */
data class AdaptiveNearMissSummaryView(
    var code: String,
    var nameText: String,
    var loadKg: String,
    var etaMinutes: String,
    var checksum: String,
    var carrierText: String,
    var revision: String,
    var batchSize: String,
    var windowDays: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toAdaptiveNearMissSummaryView(item: AdaptiveNearMissSummary): AdaptiveNearMissSummaryView {
    return AdaptiveNearMissSummaryView(
        code = truncate(item.code, 120),
        nameText = truncate(item.name, 64),
        loadKg = "%.2f".format(item.loadKg),
        etaMinutes = item.etaMinutes.toString(),
        checksum = item.checksum,
        carrierText = item.carrier,
        revision = item.revision.toString(),
        batchSize = item.batchSize.toString(),
        windowDays = item.windowDays.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNearMissSummaryViews(items: MutableList<AdaptiveNearMissSummary>): MutableList<AdaptiveNearMissSummaryView> {
    val views = mutableListOf<AdaptiveNearMissSummaryView>()
    for (item in items) {
        views.add(toAdaptiveNearMissSummaryView(item))
    }
    return views
}

fun sumAdaptiveNearMissSummaryView(view: AdaptiveNearMissSummaryView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedLoadKg + parsedEtaMinutes.toDouble() + parsedRevision.toDouble()
}
