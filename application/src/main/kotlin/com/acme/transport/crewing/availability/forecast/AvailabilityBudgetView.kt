package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability budget. */
data class AvailabilityBudgetView(
    var code: String,
    var name: String,
    var createdAt: String,
    var reviewerText: String,
    var margin: String,
    var ratio: String,
    var cost: String,
    var statusLabel: String,
)

fun toAvailabilityBudgetView(item: AvailabilityBudget): AvailabilityBudgetView {
    return AvailabilityBudgetView(
        code = item.code,
        name = truncate(item.name, 40),
        createdAt = item.createdAt.toString(),
        reviewerText = truncate(item.reviewer, 120),
        margin = "%.2f".format(item.margin),
        ratio = "%.2f".format(item.ratio),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityBudgetViews(items: MutableList<AvailabilityBudget>): MutableList<AvailabilityBudgetView> {
    val views = mutableListOf<AvailabilityBudgetView>()
    for (item in items) {
        views.add(toAvailabilityBudgetView(item))
    }
    return views
}

fun sumAvailabilityBudgetView(view: AvailabilityBudgetView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedCreatedAt.toDouble() + parsedMargin + parsedRatio
}
