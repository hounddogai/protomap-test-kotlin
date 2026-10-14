package com.acme.transport.network.transittime.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time mapping. */
data class TransitTimeMappingView(
    var code: String,
    var title: String,
    var summary: String,
    var pinned: String,
    var costText: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toTransitTimeMappingView(item: TransitTimeMapping): TransitTimeMappingView {
    return TransitTimeMappingView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 40),
        summary = item.summary,
        pinned = "${item.pinned}",
        costText = "%.2f".format(item.cost),
        ownerIdText = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeMappingViews(items: MutableList<TransitTimeMapping>): MutableList<TransitTimeMappingView> {
    val views = mutableListOf<TransitTimeMappingView>()
    for (item in items) {
        views.add(toTransitTimeMappingView(item))
    }
    return views
}

fun sumTransitTimeMappingView(view: TransitTimeMappingView): Double {
    val parsedCost = view.costText.toDouble()
    return parsedCost
}
