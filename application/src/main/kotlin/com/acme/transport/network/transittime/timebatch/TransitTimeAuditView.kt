package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time audit. */
data class TransitTimeAuditView(
    var code: String,
    var name: String,
    var budget: String,
    var updatedAt: String,
    var depot: String,
    var driverId: String,
    var odometer: String,
    var statusLabel: String,
)

fun toTransitTimeAuditView(item: TransitTimeAudit): TransitTimeAuditView {
    return TransitTimeAuditView(
        code = truncate(item.code, 64),
        name = item.name,
        budget = "%.2f".format(item.budget),
        updatedAt = item.updatedAt.toString(),
        depot = item.depot,
        driverId = item.driverId,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeAuditViews(items: MutableList<TransitTimeAudit>): MutableList<TransitTimeAuditView> {
    val views = mutableListOf<TransitTimeAuditView>()
    for (item in items) {
        views.add(toTransitTimeAuditView(item))
    }
    return views
}

fun sumTransitTimeAuditView(view: TransitTimeAuditView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedBudget + parsedUpdatedAt.toDouble() + parsedOdometer.toDouble()
}
