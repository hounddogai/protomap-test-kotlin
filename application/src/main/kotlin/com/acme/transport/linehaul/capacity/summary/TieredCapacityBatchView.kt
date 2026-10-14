package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered capacity batch. */
data class TieredCapacityBatchView(
    var codeText: String,
    var titleText: String,
    var externalRef: String,
    var minimum: String,
    var segment: String,
    var distanceKm: String,
    var priorityText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toTieredCapacityBatchView(item: TieredCapacityBatch): TieredCapacityBatchView {
    return TieredCapacityBatchView(
        codeText = item.code,
        titleText = truncate(item.title, 40),
        externalRef = truncate(item.externalRef, 40),
        minimum = "%.2f".format(item.minimum),
        segment = item.segment,
        distanceKm = "%.2f".format(item.distanceKm),
        priorityText = item.priority.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCapacityBatchViews(items: MutableList<TieredCapacityBatch>): MutableList<TieredCapacityBatchView> {
    val views = mutableListOf<TieredCapacityBatchView>()
    for (item in items) {
        views.add(toTieredCapacityBatchView(item))
    }
    return views
}

fun sumTieredCapacityBatchView(view: TieredCapacityBatchView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedMinimum + parsedDistanceKm + parsedPriority.toDouble()
}
