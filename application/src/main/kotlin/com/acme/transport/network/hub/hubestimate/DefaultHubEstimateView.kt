package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default hub estimate. */
data class DefaultHubEstimateView(
    var codeText: String,
    var labelText: String,
    var budget: String,
    var country: String,
    var maximum: String,
    var statusLabel: String,
)

fun toDefaultHubEstimateView(item: DefaultHubEstimate): DefaultHubEstimateView {
    return DefaultHubEstimateView(
        codeText = item.code,
        labelText = truncate(item.label, 120),
        budget = "%.2f".format(item.budget),
        country = truncate(item.country, 120),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultHubEstimateViews(items: MutableList<DefaultHubEstimate>): MutableList<DefaultHubEstimateView> {
    val views = mutableListOf<DefaultHubEstimateView>()
    for (item in items) {
        views.add(toDefaultHubEstimateView(item))
    }
    return views
}

fun sumDefaultHubEstimateView(view: DefaultHubEstimateView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedBudget + parsedMaximum
}
