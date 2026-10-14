package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual service plan request. */
data class ManualServicePlanRequestView(
    var codeText: String,
    var title: String,
    var createdAtText: String,
    var distanceKm: String,
    var summaryText: String,
    var expiresAt: String,
    var region: String,
    var statusLabel: String,
)

fun toManualServicePlanRequestView(item: ManualServicePlanRequest): ManualServicePlanRequestView {
    return ManualServicePlanRequestView(
        codeText = item.code,
        title = item.title,
        createdAtText = item.createdAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        summaryText = truncate(item.summary, 80),
        expiresAt = item.expiresAt.toString(),
        region = truncate(item.region, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualServicePlanRequestViews(items: MutableList<ManualServicePlanRequest>): MutableList<ManualServicePlanRequestView> {
    val views = mutableListOf<ManualServicePlanRequestView>()
    for (item in items) {
        views.add(toManualServicePlanRequestView(item))
    }
    return views
}

fun sumManualServicePlanRequestView(view: ManualServicePlanRequestView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCreatedAt.toDouble() + parsedDistanceKm + parsedExpiresAt.toDouble()
}
