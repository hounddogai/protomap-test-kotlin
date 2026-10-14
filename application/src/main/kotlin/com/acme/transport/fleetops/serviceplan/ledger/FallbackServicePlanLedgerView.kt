package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service plan ledger. */
data class FallbackServicePlanLedgerView(
    var codeText: String,
    var title: String,
    var pinnedText: String,
    var unit: String,
    var loadKg: String,
    var startsAt: String,
    var maximumText: String,
    var windowDaysText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toFallbackServicePlanLedgerView(item: FallbackServicePlanLedger): FallbackServicePlanLedgerView {
    return FallbackServicePlanLedgerView(
        codeText = item.code,
        title = item.title,
        pinnedText = "${item.pinned}",
        unit = truncate(item.unit, 64),
        loadKg = "%.2f".format(item.loadKg),
        startsAt = item.startsAt.toString(),
        maximumText = "%.2f".format(item.maximum),
        windowDaysText = item.windowDays.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServicePlanLedgerViews(items: MutableList<FallbackServicePlanLedger>): MutableList<FallbackServicePlanLedgerView> {
    val views = mutableListOf<FallbackServicePlanLedgerView>()
    for (item in items) {
        views.add(toFallbackServicePlanLedgerView(item))
    }
    return views
}

fun sumFallbackServicePlanLedgerView(view: FallbackServicePlanLedgerView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedLoadKg + parsedStartsAt.toDouble() + parsedMaximum
}
