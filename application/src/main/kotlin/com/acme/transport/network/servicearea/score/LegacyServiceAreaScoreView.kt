package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy service area score. */
data class LegacyServiceAreaScoreView(
    var code: String,
    var label: String,
    var activeText: String,
    var distanceKm: String,
    var batchSize: String,
    var etaMinutesText: String,
    var dock: String,
    var statusLabel: String,
)

fun toLegacyServiceAreaScoreView(item: LegacyServiceAreaScore): LegacyServiceAreaScoreView {
    return LegacyServiceAreaScoreView(
        code = item.code,
        label = truncate(item.label, 80),
        activeText = "${item.active}",
        distanceKm = "%.2f".format(item.distanceKm),
        batchSize = item.batchSize.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        dock = truncate(item.dock, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyServiceAreaScoreViews(items: MutableList<LegacyServiceAreaScore>): MutableList<LegacyServiceAreaScoreView> {
    val views = mutableListOf<LegacyServiceAreaScoreView>()
    for (item in items) {
        views.add(toLegacyServiceAreaScoreView(item))
    }
    return views
}

fun sumLegacyServiceAreaScoreView(view: LegacyServiceAreaScoreView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedDistanceKm + parsedBatchSize.toDouble() + parsedEtaMinutes.toDouble()
}
