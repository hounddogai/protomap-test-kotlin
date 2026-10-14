package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default attempt checkpoint. */
data class DefaultAttemptCheckpointView(
    var codeText: String,
    var label: String,
    var category: String,
    var source: String,
    var budget: String,
    var archivedText: String,
    var fuelLiters: String,
    var countryText: String,
    var statusLabel: String,
)

fun toDefaultAttemptCheckpointView(item: DefaultAttemptCheckpoint): DefaultAttemptCheckpointView {
    return DefaultAttemptCheckpointView(
        codeText = truncate(item.code, 80),
        label = item.label,
        category = truncate(item.category, 120),
        source = item.source,
        budget = "%.2f".format(item.budget),
        archivedText = "${item.archived}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAttemptCheckpointViews(items: MutableList<DefaultAttemptCheckpoint>): MutableList<DefaultAttemptCheckpointView> {
    val views = mutableListOf<DefaultAttemptCheckpointView>()
    for (item in items) {
        views.add(toDefaultAttemptCheckpointView(item))
    }
    return views
}

fun sumDefaultAttemptCheckpointView(view: DefaultAttemptCheckpointView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedBudget + parsedFuelLiters
}
