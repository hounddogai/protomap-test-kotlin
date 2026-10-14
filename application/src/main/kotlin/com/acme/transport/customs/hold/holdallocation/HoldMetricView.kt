package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold metric. */
data class HoldMetricView(
    var code: String,
    var nameText: String,
    var cost: String,
    var notesText: String,
    var visible: String,
    var categoryText: String,
    var retries: String,
    var label: String,
    var archived: String,
    var statusLabel: String,
)

fun toHoldMetricView(item: HoldMetric): HoldMetricView {
    return HoldMetricView(
        code = truncate(item.code, 120),
        nameText = item.name,
        cost = "%.2f".format(item.cost),
        notesText = item.notes,
        visible = "${item.visible}",
        categoryText = item.category,
        retries = item.retries.toString(),
        label = item.label,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldMetricViews(items: MutableList<HoldMetric>): MutableList<HoldMetricView> {
    val views = mutableListOf<HoldMetricView>()
    for (item in items) {
        views.add(toHoldMetricView(item))
    }
    return views
}

fun sumHoldMetricView(view: HoldMetricView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedCost + parsedRetries.toDouble()
}
