package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual hold estimate. */
data class ManualHoldEstimateView(
    var codeText: String,
    var titleText: String,
    var budget: String,
    var depot: String,
    var createdAtText: String,
    var driverId: String,
    var region: String,
    var statusLabel: String,
)

fun toManualHoldEstimateView(item: ManualHoldEstimate): ManualHoldEstimateView {
    return ManualHoldEstimateView(
        codeText = item.code,
        titleText = item.title,
        budget = "%.2f".format(item.budget),
        depot = item.depot,
        createdAtText = item.createdAt.toString(),
        driverId = item.driverId,
        region = truncate(item.region, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHoldEstimateViews(items: MutableList<ManualHoldEstimate>): MutableList<ManualHoldEstimateView> {
    val views = mutableListOf<ManualHoldEstimateView>()
    for (item in items) {
        views.add(toManualHoldEstimateView(item))
    }
    return views
}

fun sumManualHoldEstimateView(view: ManualHoldEstimateView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedBudget + parsedCreatedAt.toDouble()
}
