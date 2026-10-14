package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary service plan mapping. */
data class PrimaryServicePlanMappingView(
    var code: String,
    var title: String,
    var stopCountText: String,
    var updatedAt: String,
    var archivedText: String,
    var ratio: String,
    var depot: String,
    var activeText: String,
    var minimumText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toPrimaryServicePlanMappingView(item: PrimaryServicePlanMapping): PrimaryServicePlanMappingView {
    return PrimaryServicePlanMappingView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 80),
        stopCountText = item.stopCount.toString(),
        updatedAt = item.updatedAt.toString(),
        archivedText = "${item.archived}",
        ratio = "%.2f".format(item.ratio),
        depot = item.depot,
        activeText = "${item.active}",
        minimumText = "%.2f".format(item.minimum),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryServicePlanMappingViews(items: MutableList<PrimaryServicePlanMapping>): MutableList<PrimaryServicePlanMappingView> {
    val views = mutableListOf<PrimaryServicePlanMappingView>()
    for (item in items) {
        views.add(toPrimaryServicePlanMappingView(item))
    }
    return views
}

fun sumPrimaryServicePlanMappingView(view: PrimaryServicePlanMappingView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedStopCount.toDouble() + parsedUpdatedAt.toDouble() + parsedRatio
}
