package com.acme.transport.network.node.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted node threshold. */
data class WeightedNodeThresholdView(
    var code: String,
    var label: String,
    var unit: String,
    var startsAtText: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toWeightedNodeThresholdView(item: WeightedNodeThreshold): WeightedNodeThresholdView {
    return WeightedNodeThresholdView(
        code = item.code,
        label = truncate(item.label, 40),
        unit = item.unit,
        startsAtText = item.startsAt.toString(),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedNodeThresholdViews(items: MutableList<WeightedNodeThreshold>): MutableList<WeightedNodeThresholdView> {
    val views = mutableListOf<WeightedNodeThresholdView>()
    for (item in items) {
        views.add(toWeightedNodeThresholdView(item))
    }
    return views
}

fun sumWeightedNodeThresholdView(view: WeightedNodeThresholdView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedStartsAt.toDouble()
}
