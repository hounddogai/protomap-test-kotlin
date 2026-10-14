package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient emission factor request. */
data class LenientEmissionFactorRequestView(
    var code: String,
    var name: String,
    var budgetText: String,
    var attemptsText: String,
    var descriptionText: String,
    var capacity: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toLenientEmissionFactorRequestView(item: LenientEmissionFactorRequest): LenientEmissionFactorRequestView {
    return LenientEmissionFactorRequestView(
        code = item.code,
        name = truncate(item.name, 40),
        budgetText = "%.2f".format(item.budget),
        attemptsText = item.attempts.toString(),
        descriptionText = item.description,
        capacity = item.capacity.toString(),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientEmissionFactorRequestViews(items: MutableList<LenientEmissionFactorRequest>): MutableList<LenientEmissionFactorRequestView> {
    val views = mutableListOf<LenientEmissionFactorRequestView>()
    for (item in items) {
        views.add(toLenientEmissionFactorRequestView(item))
    }
    return views
}

fun sumLenientEmissionFactorRequestView(view: LenientEmissionFactorRequestView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedBudget + parsedAttempts.toDouble() + parsedCapacity.toDouble()
}
