package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global sortation draft. */
data class GlobalSortationDraftView(
    var code: String,
    var title: String,
    var ownerId: String,
    var phone: String,
    var distanceKm: String,
    var trailerId: String,
    var carrier: String,
    var driverId: String,
    var cost: String,
    var segmentText: String,
    var visible: String,
    var statusLabel: String,
)

fun toGlobalSortationDraftView(item: GlobalSortationDraft): GlobalSortationDraftView {
    return GlobalSortationDraftView(
        code = item.code,
        title = item.title,
        ownerId = item.ownerId,
        phone = truncate(item.phone, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        trailerId = item.trailerId,
        carrier = item.carrier,
        driverId = item.driverId,
        cost = "%.2f".format(item.cost),
        segmentText = item.segment,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalSortationDraftViews(items: MutableList<GlobalSortationDraft>): MutableList<GlobalSortationDraftView> {
    val views = mutableListOf<GlobalSortationDraftView>()
    for (item in items) {
        views.add(toGlobalSortationDraftView(item))
    }
    return views
}

fun sumGlobalSortationDraftView(view: GlobalSortationDraftView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedDistanceKm + parsedCost
}
