package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted sortation estimate. */
data class WeightedSortationEstimateView(
    var codeText: String,
    var label: String,
    var lane: String,
    var createdAt: String,
    var versionText: String,
    var visibleText: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toWeightedSortationEstimateView(item: WeightedSortationEstimate): WeightedSortationEstimateView {
    return WeightedSortationEstimateView(
        codeText = item.code,
        label = item.label,
        lane = truncate(item.lane, 80),
        createdAt = item.createdAt.toString(),
        versionText = item.version.toString(),
        visibleText = "${item.visible}",
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedSortationEstimateViews(items: MutableList<WeightedSortationEstimate>): MutableList<WeightedSortationEstimateView> {
    val views = mutableListOf<WeightedSortationEstimateView>()
    for (item in items) {
        views.add(toWeightedSortationEstimateView(item))
    }
    return views
}

fun sumWeightedSortationEstimateView(view: WeightedSortationEstimateView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedCreatedAt.toDouble() + parsedVersion.toDouble() + parsedBudget
}
