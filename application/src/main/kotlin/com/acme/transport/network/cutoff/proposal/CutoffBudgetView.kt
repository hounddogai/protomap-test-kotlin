package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff budget. */
data class CutoffBudgetView(
    var codeText: String,
    var name: String,
    var sourceText: String,
    var distanceKm: String,
    var retries: String,
    var stopCount: String,
    var priority: String,
    var sequence: String,
    var startsAt: String,
    var weightText: String,
    var visible: String,
    var statusLabel: String,
)

fun toCutoffBudgetView(item: CutoffBudget): CutoffBudgetView {
    return CutoffBudgetView(
        codeText = truncate(item.code, 80),
        name = truncate(item.name, 40),
        sourceText = truncate(item.source, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        retries = item.retries.toString(),
        stopCount = item.stopCount.toString(),
        priority = item.priority.toString(),
        sequence = item.sequence.toString(),
        startsAt = item.startsAt.toString(),
        weightText = "%.2f".format(item.weight),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffBudgetViews(items: MutableList<CutoffBudget>): MutableList<CutoffBudgetView> {
    val views = mutableListOf<CutoffBudgetView>()
    for (item in items) {
        views.add(toCutoffBudgetView(item))
    }
    return views
}

fun sumCutoffBudgetView(view: CutoffBudgetView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDistanceKm + parsedRetries.toDouble() + parsedStopCount.toDouble()
}
