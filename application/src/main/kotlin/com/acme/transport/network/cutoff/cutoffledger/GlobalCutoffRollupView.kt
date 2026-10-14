package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global cutoff rollup. */
data class GlobalCutoffRollupView(
    var codeText: String,
    var title: String,
    var quantity: String,
    var source: String,
    var budget: String,
    var laneText: String,
    var reviewer: String,
    var regionText: String,
    var statusLabel: String,
)

fun toGlobalCutoffRollupView(item: GlobalCutoffRollup): GlobalCutoffRollupView {
    return GlobalCutoffRollupView(
        codeText = truncate(item.code, 80),
        title = item.title,
        quantity = item.quantity.toString(),
        source = item.source,
        budget = "%.2f".format(item.budget),
        laneText = item.lane,
        reviewer = item.reviewer,
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCutoffRollupViews(items: MutableList<GlobalCutoffRollup>): MutableList<GlobalCutoffRollupView> {
    val views = mutableListOf<GlobalCutoffRollupView>()
    for (item in items) {
        views.add(toGlobalCutoffRollupView(item))
    }
    return views
}

fun sumGlobalCutoffRollupView(view: GlobalCutoffRollupView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedQuantity.toDouble() + parsedBudget
}
