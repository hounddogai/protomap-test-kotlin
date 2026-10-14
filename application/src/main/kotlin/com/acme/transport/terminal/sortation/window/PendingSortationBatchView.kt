package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending sortation batch. */
data class PendingSortationBatchView(
    var code: String,
    var name: String,
    var routeCodeText: String,
    var visible: String,
    var ceilingText: String,
    var costText: String,
    var minimum: String,
    var priorityText: String,
    var region: String,
    var shiftCode: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toPendingSortationBatchView(item: PendingSortationBatch): PendingSortationBatchView {
    return PendingSortationBatchView(
        code = item.code,
        name = item.name,
        routeCodeText = item.routeCode,
        visible = "${item.visible}",
        ceilingText = "%.2f".format(item.ceiling),
        costText = "%.2f".format(item.cost),
        minimum = "%.2f".format(item.minimum),
        priorityText = item.priority.toString(),
        region = truncate(item.region, 64),
        shiftCode = item.shiftCode,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingSortationBatchViews(items: MutableList<PendingSortationBatch>): MutableList<PendingSortationBatchView> {
    val views = mutableListOf<PendingSortationBatchView>()
    for (item in items) {
        views.add(toPendingSortationBatchView(item))
    }
    return views
}

fun sumPendingSortationBatchView(view: PendingSortationBatchView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedCost = view.costText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCeiling + parsedCost + parsedMinimum
}
