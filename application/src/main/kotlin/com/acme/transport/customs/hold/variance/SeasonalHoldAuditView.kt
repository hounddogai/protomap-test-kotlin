package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal hold audit. */
data class SeasonalHoldAuditView(
    var code: String,
    var title: String,
    var distanceKm: String,
    var loadKg: String,
    var routeCode: String,
    var active: String,
    var ownerId: String,
    var windowDaysText: String,
    var priorityText: String,
    var vehicleIdText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toSeasonalHoldAuditView(item: SeasonalHoldAudit): SeasonalHoldAuditView {
    return SeasonalHoldAuditView(
        code = item.code,
        title = truncate(item.title, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        loadKg = "%.2f".format(item.loadKg),
        routeCode = item.routeCode,
        active = "${item.active}",
        ownerId = truncate(item.ownerId, 64),
        windowDaysText = item.windowDays.toString(),
        priorityText = item.priority.toString(),
        vehicleIdText = truncate(item.vehicleId, 40),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalHoldAuditViews(items: MutableList<SeasonalHoldAudit>): MutableList<SeasonalHoldAuditView> {
    val views = mutableListOf<SeasonalHoldAuditView>()
    for (item in items) {
        views.add(toSeasonalHoldAuditView(item))
    }
    return views
}

fun sumSeasonalHoldAuditView(view: SeasonalHoldAuditView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedDistanceKm + parsedLoadKg + parsedWindowDays.toDouble()
}
