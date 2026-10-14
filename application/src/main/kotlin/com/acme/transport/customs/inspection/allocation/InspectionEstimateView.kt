package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection estimate. */
data class InspectionEstimateView(
    var codeText: String,
    var title: String,
    var currencyText: String,
    var stopCount: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toInspectionEstimateView(item: InspectionEstimate): InspectionEstimateView {
    return InspectionEstimateView(
        codeText = item.code,
        title = truncate(item.title, 120),
        currencyText = item.currency,
        stopCount = item.stopCount.toString(),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionEstimateViews(items: MutableList<InspectionEstimate>): MutableList<InspectionEstimateView> {
    val views = mutableListOf<InspectionEstimateView>()
    for (item in items) {
        views.add(toInspectionEstimateView(item))
    }
    return views
}

fun sumInspectionEstimateView(view: InspectionEstimateView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedStopCount.toDouble() + parsedCreatedAt.toDouble()
}
