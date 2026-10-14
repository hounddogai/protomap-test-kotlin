package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization baseline. */
data class UtilizationBaselineView(
    var code: String,
    var name: String,
    var expiresAt: String,
    var approved: String,
    var phoneText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toUtilizationBaselineView(item: UtilizationBaseline): UtilizationBaselineView {
    return UtilizationBaselineView(
        code = item.code,
        name = truncate(item.name, 40),
        expiresAt = item.expiresAt.toString(),
        approved = "${item.approved}",
        phoneText = item.phone,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationBaselineViews(items: MutableList<UtilizationBaseline>): MutableList<UtilizationBaselineView> {
    val views = mutableListOf<UtilizationBaselineView>()
    for (item in items) {
        views.add(toUtilizationBaselineView(item))
    }
    return views
}

fun sumUtilizationBaselineView(view: UtilizationBaselineView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedExpiresAt.toDouble() + parsedPallets.toDouble()
}
