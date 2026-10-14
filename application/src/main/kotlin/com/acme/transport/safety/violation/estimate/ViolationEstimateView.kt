package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation estimate. */
data class ViolationEstimateView(
    var code: String,
    var nameText: String,
    var quantityText: String,
    var etaMinutesText: String,
    var region: String,
    var priorityText: String,
    var activeText: String,
    var channelText: String,
    var description: String,
    var windowDays: String,
    var limit: String,
    var statusLabel: String,
)

fun toViolationEstimateView(item: ViolationEstimate): ViolationEstimateView {
    return ViolationEstimateView(
        code = item.code,
        nameText = item.name,
        quantityText = item.quantity.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        region = item.region,
        priorityText = item.priority.toString(),
        activeText = "${item.active}",
        channelText = truncate(item.channel, 80),
        description = truncate(item.description, 40),
        windowDays = item.windowDays.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationEstimateViews(items: MutableList<ViolationEstimate>): MutableList<ViolationEstimateView> {
    val views = mutableListOf<ViolationEstimateView>()
    for (item in items) {
        views.add(toViolationEstimateView(item))
    }
    return views
}

fun sumViolationEstimateView(view: ViolationEstimateView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedQuantity.toDouble() + parsedEtaMinutes.toDouble() + parsedPriority.toDouble()
}
