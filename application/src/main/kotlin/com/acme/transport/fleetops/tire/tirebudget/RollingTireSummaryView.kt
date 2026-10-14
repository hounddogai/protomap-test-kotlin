package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tire summary. */
data class RollingTireSummaryView(
    var code: String,
    var labelText: String,
    var updatedAt: String,
    var vehicleId: String,
    var currency: String,
    var amountText: String,
    var statusLabel: String,
)

fun toRollingTireSummaryView(item: RollingTireSummary): RollingTireSummaryView {
    return RollingTireSummaryView(
        code = item.code,
        labelText = truncate(item.label, 64),
        updatedAt = item.updatedAt.toString(),
        vehicleId = item.vehicleId,
        currency = truncate(item.currency, 80),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTireSummaryViews(items: MutableList<RollingTireSummary>): MutableList<RollingTireSummaryView> {
    val views = mutableListOf<RollingTireSummaryView>()
    for (item in items) {
        views.add(toRollingTireSummaryView(item))
    }
    return views
}

fun sumRollingTireSummaryView(view: RollingTireSummaryView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedAmount
}
