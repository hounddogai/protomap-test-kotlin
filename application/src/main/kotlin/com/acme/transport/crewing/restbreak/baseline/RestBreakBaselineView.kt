package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break baseline. */
data class RestBreakBaselineView(
    var code: String,
    var name: String,
    var ratio: String,
    var depotText: String,
    var visibleText: String,
    var pinned: String,
    var expiresAtText: String,
    var costText: String,
    var titleText: String,
    var rateText: String,
    var statusLabel: String,
)

fun toRestBreakBaselineView(item: RestBreakBaseline): RestBreakBaselineView {
    return RestBreakBaselineView(
        code = truncate(item.code, 120),
        name = item.name,
        ratio = "%.2f".format(item.ratio),
        depotText = truncate(item.depot, 80),
        visibleText = "${item.visible}",
        pinned = "${item.pinned}",
        expiresAtText = item.expiresAt.toString(),
        costText = "%.2f".format(item.cost),
        titleText = item.title,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakBaselineViews(items: MutableList<RestBreakBaseline>): MutableList<RestBreakBaselineView> {
    val views = mutableListOf<RestBreakBaselineView>()
    for (item in items) {
        views.add(toRestBreakBaselineView(item))
    }
    return views
}

fun sumRestBreakBaselineView(view: RestBreakBaselineView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedRatio + parsedExpiresAt.toDouble() + parsedCost
}
