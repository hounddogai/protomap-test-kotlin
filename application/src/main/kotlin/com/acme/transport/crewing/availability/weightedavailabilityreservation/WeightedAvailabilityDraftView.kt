package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted availability draft. */
data class WeightedAvailabilityDraftView(
    var code: String,
    var nameText: String,
    var currency: String,
    var attemptsText: String,
    var fuelLiters: String,
    var lane: String,
    var pinnedText: String,
    var budget: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toWeightedAvailabilityDraftView(item: WeightedAvailabilityDraft): WeightedAvailabilityDraftView {
    return WeightedAvailabilityDraftView(
        code = item.code,
        nameText = truncate(item.name, 120),
        currency = item.currency,
        attemptsText = item.attempts.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        lane = item.lane,
        pinnedText = "${item.pinned}",
        budget = "%.2f".format(item.budget),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAvailabilityDraftViews(items: MutableList<WeightedAvailabilityDraft>): MutableList<WeightedAvailabilityDraftView> {
    val views = mutableListOf<WeightedAvailabilityDraftView>()
    for (item in items) {
        views.add(toWeightedAvailabilityDraftView(item))
    }
    return views
}

fun sumWeightedAvailabilityDraftView(view: WeightedAvailabilityDraftView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedAttempts.toDouble() + parsedFuelLiters + parsedBudget
}
