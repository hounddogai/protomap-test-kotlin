package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization draft. */
data class UtilizationDraftView(
    var codeText: String,
    var labelText: String,
    var carrier: String,
    var approved: String,
    var amount: String,
    var active: String,
    var routeCodeText: String,
    var versionText: String,
    var driverId: String,
    var statusLabel: String,
)

fun toUtilizationDraftView(item: UtilizationDraft): UtilizationDraftView {
    return UtilizationDraftView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        carrier = item.carrier,
        approved = "${item.approved}",
        amount = "%.2f".format(item.amount),
        active = "${item.active}",
        routeCodeText = item.routeCode,
        versionText = item.version.toString(),
        driverId = truncate(item.driverId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationDraftViews(items: MutableList<UtilizationDraft>): MutableList<UtilizationDraftView> {
    val views = mutableListOf<UtilizationDraftView>()
    for (item in items) {
        views.add(toUtilizationDraftView(item))
    }
    return views
}

fun sumUtilizationDraftView(view: UtilizationDraftView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedAmount + parsedVersion.toDouble()
}
