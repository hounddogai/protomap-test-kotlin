package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default cutoff estimate. */
data class DefaultCutoffEstimateView(
    var code: String,
    var title: String,
    var floorValueText: String,
    var country: String,
    var budget: String,
    var activeText: String,
    var segment: String,
    var statusLabel: String,
)

fun toDefaultCutoffEstimateView(item: DefaultCutoffEstimate): DefaultCutoffEstimateView {
    return DefaultCutoffEstimateView(
        code = item.code,
        title = truncate(item.title, 40),
        floorValueText = "%.2f".format(item.floorValue),
        country = truncate(item.country, 80),
        budget = "%.2f".format(item.budget),
        activeText = "${item.active}",
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCutoffEstimateViews(items: MutableList<DefaultCutoffEstimate>): MutableList<DefaultCutoffEstimateView> {
    val views = mutableListOf<DefaultCutoffEstimateView>()
    for (item in items) {
        views.add(toDefaultCutoffEstimateView(item))
    }
    return views
}

fun sumDefaultCutoffEstimateView(view: DefaultCutoffEstimateView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedFloorValue + parsedBudget
}
