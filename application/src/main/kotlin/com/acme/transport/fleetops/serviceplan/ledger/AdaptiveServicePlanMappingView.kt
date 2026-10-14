package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive service plan mapping. */
data class AdaptiveServicePlanMappingView(
    var code: String,
    var label: String,
    var segment: String,
    var retries: String,
    var routeCode: String,
    var budget: String,
    var titleText: String,
    var statusLabel: String,
)

fun toAdaptiveServicePlanMappingView(item: AdaptiveServicePlanMapping): AdaptiveServicePlanMappingView {
    return AdaptiveServicePlanMappingView(
        code = item.code,
        label = item.label,
        segment = truncate(item.segment, 120),
        retries = item.retries.toString(),
        routeCode = truncate(item.routeCode, 64),
        budget = "%.2f".format(item.budget),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveServicePlanMappingViews(items: MutableList<AdaptiveServicePlanMapping>): MutableList<AdaptiveServicePlanMappingView> {
    val views = mutableListOf<AdaptiveServicePlanMappingView>()
    for (item in items) {
        views.add(toAdaptiveServicePlanMappingView(item))
    }
    return views
}

fun sumAdaptiveServicePlanMappingView(view: AdaptiveServicePlanMappingView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRetries.toDouble() + parsedBudget
}
