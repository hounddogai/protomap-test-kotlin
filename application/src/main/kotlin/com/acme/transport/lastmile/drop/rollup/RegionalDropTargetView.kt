package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional drop target. */
data class RegionalDropTargetView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var updatedAt: String,
    var minimum: String,
    var trailerIdText: String,
    var visible: String,
    var sourceText: String,
    var summary: String,
    var statusLabel: String,
)

fun toRegionalDropTargetView(item: RegionalDropTarget): RegionalDropTargetView {
    return RegionalDropTargetView(
        code = item.code,
        titleText = truncate(item.title, 64),
        etaMinutes = item.etaMinutes.toString(),
        updatedAt = item.updatedAt.toString(),
        minimum = "%.2f".format(item.minimum),
        trailerIdText = item.trailerId,
        visible = "${item.visible}",
        sourceText = truncate(item.source, 40),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDropTargetViews(items: MutableList<RegionalDropTarget>): MutableList<RegionalDropTargetView> {
    val views = mutableListOf<RegionalDropTargetView>()
    for (item in items) {
        views.add(toRegionalDropTargetView(item))
    }
    return views
}

fun sumRegionalDropTargetView(view: RegionalDropTargetView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedEtaMinutes.toDouble() + parsedUpdatedAt.toDouble() + parsedMinimum
}
