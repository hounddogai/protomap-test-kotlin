package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional service area budget. */
data class RegionalServiceAreaBudgetView(
    var code: String,
    var label: String,
    var weight: String,
    var sequence: String,
    var ratio: String,
    var statusLabel: String,
)

fun toRegionalServiceAreaBudgetView(item: RegionalServiceAreaBudget): RegionalServiceAreaBudgetView {
    return RegionalServiceAreaBudgetView(
        code = truncate(item.code, 64),
        label = item.label,
        weight = "%.2f".format(item.weight),
        sequence = item.sequence.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalServiceAreaBudgetViews(items: MutableList<RegionalServiceAreaBudget>): MutableList<RegionalServiceAreaBudgetView> {
    val views = mutableListOf<RegionalServiceAreaBudgetView>()
    for (item in items) {
        views.add(toRegionalServiceAreaBudgetView(item))
    }
    return views
}

fun sumRegionalServiceAreaBudgetView(view: RegionalServiceAreaBudgetView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedWeight + parsedSequence.toDouble() + parsedRatio
}
