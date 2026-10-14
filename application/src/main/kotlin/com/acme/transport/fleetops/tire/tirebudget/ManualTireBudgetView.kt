package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tire budget. */
data class ManualTireBudgetView(
    var code: String,
    var name: String,
    var summary: String,
    var trailerId: String,
    var durationMinutesText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toManualTireBudgetView(item: ManualTireBudget): ManualTireBudgetView {
    return ManualTireBudgetView(
        code = item.code,
        name = item.name,
        summary = item.summary,
        trailerId = truncate(item.trailerId, 40),
        durationMinutesText = item.durationMinutes.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTireBudgetViews(items: MutableList<ManualTireBudget>): MutableList<ManualTireBudgetView> {
    val views = mutableListOf<ManualTireBudgetView>()
    for (item in items) {
        views.add(toManualTireBudgetView(item))
    }
    return views
}

fun sumManualTireBudgetView(view: ManualTireBudgetView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedDurationMinutes.toDouble() + parsedCreatedAt.toDouble()
}
