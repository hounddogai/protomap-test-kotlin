package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched emission factor policy. */
data class BatchedEmissionFactorPolicyView(
    var code: String,
    var title: String,
    var archived: String,
    var externalRef: String,
    var vehicleId: String,
    var currencyText: String,
    var loadKg: String,
    var cost: String,
    var scoreText: String,
    var etaMinutes: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toBatchedEmissionFactorPolicyView(item: BatchedEmissionFactorPolicy): BatchedEmissionFactorPolicyView {
    return BatchedEmissionFactorPolicyView(
        code = item.code,
        title = truncate(item.title, 80),
        archived = "${item.archived}",
        externalRef = truncate(item.externalRef, 80),
        vehicleId = item.vehicleId,
        currencyText = truncate(item.currency, 120),
        loadKg = "%.2f".format(item.loadKg),
        cost = "%.2f".format(item.cost),
        scoreText = "%.2f".format(item.score),
        etaMinutes = item.etaMinutes.toString(),
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEmissionFactorPolicyViews(items: MutableList<BatchedEmissionFactorPolicy>): MutableList<BatchedEmissionFactorPolicyView> {
    val views = mutableListOf<BatchedEmissionFactorPolicyView>()
    for (item in items) {
        views.add(toBatchedEmissionFactorPolicyView(item))
    }
    return views
}

fun sumBatchedEmissionFactorPolicyView(view: BatchedEmissionFactorPolicyView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedLoadKg + parsedCost + parsedScore
}
