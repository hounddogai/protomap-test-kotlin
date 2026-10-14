package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation group. */
data class SortationGroupView(
    var code: String,
    var title: String,
    var batchSize: String,
    var capacity: String,
    var costText: String,
    var unit: String,
    var segment: String,
    var trailerId: String,
    var marginText: String,
    var statusLabel: String,
)

fun toSortationGroupView(item: SortationGroup): SortationGroupView {
    return SortationGroupView(
        code = item.code,
        title = truncate(item.title, 120),
        batchSize = item.batchSize.toString(),
        capacity = item.capacity.toString(),
        costText = "%.2f".format(item.cost),
        unit = item.unit,
        segment = item.segment,
        trailerId = item.trailerId,
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationGroupViews(items: MutableList<SortationGroup>): MutableList<SortationGroupView> {
    val views = mutableListOf<SortationGroupView>()
    for (item in items) {
        views.add(toSortationGroupView(item))
    }
    return views
}

fun sumSortationGroupView(view: SortationGroupView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedBatchSize.toDouble() + parsedCapacity.toDouble() + parsedCost
}
