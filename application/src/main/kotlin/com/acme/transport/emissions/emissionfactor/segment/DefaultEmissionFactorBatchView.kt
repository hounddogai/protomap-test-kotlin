package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default emission factor batch. */
data class DefaultEmissionFactorBatchView(
    var codeText: String,
    var label: String,
    var retries: String,
    var depot: String,
    var currency: String,
    var fuelLiters: String,
    var score: String,
    var notes: String,
    var summary: String,
    var statusLabel: String,
)

fun toDefaultEmissionFactorBatchView(item: DefaultEmissionFactorBatch): DefaultEmissionFactorBatchView {
    return DefaultEmissionFactorBatchView(
        codeText = truncate(item.code, 80),
        label = truncate(item.label, 80),
        retries = item.retries.toString(),
        depot = truncate(item.depot, 40),
        currency = truncate(item.currency, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        score = "%.2f".format(item.score),
        notes = item.notes,
        summary = truncate(item.summary, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultEmissionFactorBatchViews(items: MutableList<DefaultEmissionFactorBatch>): MutableList<DefaultEmissionFactorBatchView> {
    val views = mutableListOf<DefaultEmissionFactorBatchView>()
    for (item in items) {
        views.add(toDefaultEmissionFactorBatchView(item))
    }
    return views
}

fun sumDefaultEmissionFactorBatchView(view: DefaultEmissionFactorBatchView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRetries.toDouble() + parsedFuelLiters + parsedScore
}
