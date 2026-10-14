package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted accessorial allocation. */
data class WeightedAccessorialAllocationView(
    var code: String,
    var label: String,
    var locale: String,
    var threshold: String,
    var priority: String,
    var limit: String,
    var minimumText: String,
    var name: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toWeightedAccessorialAllocationView(item: WeightedAccessorialAllocation): WeightedAccessorialAllocationView {
    return WeightedAccessorialAllocationView(
        code = truncate(item.code, 40),
        label = item.label,
        locale = truncate(item.locale, 40),
        threshold = "%.2f".format(item.threshold),
        priority = item.priority.toString(),
        limit = item.limit.toString(),
        minimumText = "%.2f".format(item.minimum),
        name = truncate(item.name, 40),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAccessorialAllocationViews(items: MutableList<WeightedAccessorialAllocation>): MutableList<WeightedAccessorialAllocationView> {
    val views = mutableListOf<WeightedAccessorialAllocationView>()
    for (item in items) {
        views.add(toWeightedAccessorialAllocationView(item))
    }
    return views
}

fun sumWeightedAccessorialAllocationView(view: WeightedAccessorialAllocationView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedThreshold + parsedPriority.toDouble() + parsedLimit.toDouble()
}
