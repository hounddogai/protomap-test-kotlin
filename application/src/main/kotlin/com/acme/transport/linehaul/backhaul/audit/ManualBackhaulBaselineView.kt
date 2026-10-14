package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual backhaul baseline. */
data class ManualBackhaulBaselineView(
    var code: String,
    var label: String,
    var rateText: String,
    var unit: String,
    var amountText: String,
    var retries: String,
    var active: String,
    var vehicleId: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toManualBackhaulBaselineView(item: ManualBackhaulBaseline): ManualBackhaulBaselineView {
    return ManualBackhaulBaselineView(
        code = item.code,
        label = truncate(item.label, 120),
        rateText = "%.2f".format(item.rate),
        unit = item.unit,
        amountText = "%.2f".format(item.amount),
        retries = item.retries.toString(),
        active = "${item.active}",
        vehicleId = item.vehicleId,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualBackhaulBaselineViews(items: MutableList<ManualBackhaulBaseline>): MutableList<ManualBackhaulBaselineView> {
    val views = mutableListOf<ManualBackhaulBaselineView>()
    for (item in items) {
        views.add(toManualBackhaulBaselineView(item))
    }
    return views
}

fun sumManualBackhaulBaselineView(view: ManualBackhaulBaselineView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedAmount = view.amountText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedRate + parsedAmount + parsedRetries.toDouble()
}
