package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted emission factor batch. */
data class WeightedEmissionFactorBatchView(
    var codeText: String,
    var nameText: String,
    var pinnedText: String,
    var etaMinutes: String,
    var approved: String,
    var startsAt: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toWeightedEmissionFactorBatchView(item: WeightedEmissionFactorBatch): WeightedEmissionFactorBatchView {
    return WeightedEmissionFactorBatchView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        pinnedText = "${item.pinned}",
        etaMinutes = item.etaMinutes.toString(),
        approved = "${item.approved}",
        startsAt = item.startsAt.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEmissionFactorBatchViews(items: MutableList<WeightedEmissionFactorBatch>): MutableList<WeightedEmissionFactorBatchView> {
    val views = mutableListOf<WeightedEmissionFactorBatchView>()
    for (item in items) {
        views.add(toWeightedEmissionFactorBatchView(item))
    }
    return views
}

fun sumWeightedEmissionFactorBatchView(view: WeightedEmissionFactorBatchView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedStartsAt.toDouble() + parsedDistanceKm
}
