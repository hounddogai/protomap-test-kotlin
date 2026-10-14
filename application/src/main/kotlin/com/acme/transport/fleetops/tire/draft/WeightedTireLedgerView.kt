package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted tire ledger. */
data class WeightedTireLedgerView(
    var codeText: String,
    var name: String,
    var countryText: String,
    var category: String,
    var endsAtText: String,
    var durationMinutes: String,
    var pinned: String,
    var weight: String,
    var statusLabel: String,
)

fun toWeightedTireLedgerView(item: WeightedTireLedger): WeightedTireLedgerView {
    return WeightedTireLedgerView(
        codeText = truncate(item.code, 80),
        name = item.name,
        countryText = item.country,
        category = truncate(item.category, 120),
        endsAtText = item.endsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        pinned = "${item.pinned}",
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTireLedgerViews(items: MutableList<WeightedTireLedger>): MutableList<WeightedTireLedgerView> {
    val views = mutableListOf<WeightedTireLedgerView>()
    for (item in items) {
        views.add(toWeightedTireLedgerView(item))
    }
    return views
}

fun sumWeightedTireLedgerView(view: WeightedTireLedgerView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedEndsAt.toDouble() + parsedDurationMinutes.toDouble() + parsedWeight
}
