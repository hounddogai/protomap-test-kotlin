package com.acme.transport.lastmile.attempt.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt audit. */
data class AttemptAuditView(
    var codeText: String,
    var title: String,
    var timezone: String,
    var windowDays: String,
    var driverId: String,
    var pallets: String,
    var fuelLiters: String,
    var name: String,
    var archived: String,
    var amount: String,
    var priority: String,
    var statusLabel: String,
)

fun toAttemptAuditView(item: AttemptAudit): AttemptAuditView {
    return AttemptAuditView(
        codeText = item.code,
        title = item.title,
        timezone = truncate(item.timezone, 80),
        windowDays = item.windowDays.toString(),
        driverId = item.driverId,
        pallets = item.pallets.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        name = item.name,
        archived = "${item.archived}",
        amount = "%.2f".format(item.amount),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptAuditViews(items: MutableList<AttemptAudit>): MutableList<AttemptAuditView> {
    val views = mutableListOf<AttemptAuditView>()
    for (item in items) {
        views.add(toAttemptAuditView(item))
    }
    return views
}

fun sumAttemptAuditView(view: AttemptAuditView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedWindowDays.toDouble() + parsedPallets.toDouble() + parsedFuelLiters
}
