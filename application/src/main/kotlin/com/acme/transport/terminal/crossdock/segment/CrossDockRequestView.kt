package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock request. */
data class CrossDockRequestView(
    var code: String,
    var label: String,
    var revisionText: String,
    var dockText: String,
    var ceiling: String,
    var cost: String,
    var ratio: String,
    var statusLabel: String,
)

fun toCrossDockRequestView(item: CrossDockRequest): CrossDockRequestView {
    return CrossDockRequestView(
        code = item.code,
        label = item.label,
        revisionText = item.revision.toString(),
        dockText = truncate(item.dock, 40),
        ceiling = "%.2f".format(item.ceiling),
        cost = "%.2f".format(item.cost),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockRequestViews(items: MutableList<CrossDockRequest>): MutableList<CrossDockRequestView> {
    val views = mutableListOf<CrossDockRequestView>()
    for (item in items) {
        views.add(toCrossDockRequestView(item))
    }
    return views
}

fun sumCrossDockRequestView(view: CrossDockRequestView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedRevision.toDouble() + parsedCeiling + parsedCost
}
