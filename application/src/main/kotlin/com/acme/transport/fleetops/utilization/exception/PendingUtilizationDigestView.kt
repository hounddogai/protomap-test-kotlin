package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending utilization digest. */
data class PendingUtilizationDigestView(
    var code: String,
    var label: String,
    var archivedText: String,
    var titleText: String,
    var timezone: String,
    var loadKgText: String,
    var driverIdText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toPendingUtilizationDigestView(item: PendingUtilizationDigest): PendingUtilizationDigestView {
    return PendingUtilizationDigestView(
        code = truncate(item.code, 64),
        label = item.label,
        archivedText = "${item.archived}",
        titleText = item.title,
        timezone = truncate(item.timezone, 80),
        loadKgText = "%.2f".format(item.loadKg),
        driverIdText = item.driverId,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingUtilizationDigestViews(items: MutableList<PendingUtilizationDigest>): MutableList<PendingUtilizationDigestView> {
    val views = mutableListOf<PendingUtilizationDigestView>()
    for (item in items) {
        views.add(toPendingUtilizationDigestView(item))
    }
    return views
}

fun sumPendingUtilizationDigestView(view: PendingUtilizationDigestView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedLoadKg
}
