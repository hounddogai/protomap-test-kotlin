package com.acme.transport.customs.clearance.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance mapping. */
data class ClearanceMappingView(
    var code: String,
    var titleText: String,
    var capacity: String,
    var limit: String,
    var retriesText: String,
    var approved: String,
    var budgetText: String,
    var rate: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toClearanceMappingView(item: ClearanceMapping): ClearanceMappingView {
    return ClearanceMappingView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 80),
        capacity = item.capacity.toString(),
        limit = item.limit.toString(),
        retriesText = item.retries.toString(),
        approved = "${item.approved}",
        budgetText = "%.2f".format(item.budget),
        rate = "%.2f".format(item.rate),
        carrierText = truncate(item.carrier, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceMappingViews(items: MutableList<ClearanceMapping>): MutableList<ClearanceMappingView> {
    val views = mutableListOf<ClearanceMappingView>()
    for (item in items) {
        views.add(toClearanceMappingView(item))
    }
    return views
}

fun sumClearanceMappingView(view: ClearanceMappingView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedCapacity.toDouble() + parsedLimit.toDouble() + parsedRetries.toDouble()
}
