package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual ev charge audit. */
data class ManualEvChargeAuditView(
    var code: String,
    var titleText: String,
    var shiftCodeText: String,
    var approvedText: String,
    var threshold: String,
    var capacity: String,
    var locale: String,
    var fuelLiters: String,
    var pallets: String,
    var statusLabel: String,
)

fun toManualEvChargeAuditView(item: ManualEvChargeAudit): ManualEvChargeAuditView {
    return ManualEvChargeAuditView(
        code = truncate(item.code, 64),
        titleText = item.title,
        shiftCodeText = truncate(item.shiftCode, 64),
        approvedText = "${item.approved}",
        threshold = "%.2f".format(item.threshold),
        capacity = item.capacity.toString(),
        locale = item.locale,
        fuelLiters = "%.2f".format(item.fuelLiters),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualEvChargeAuditViews(items: MutableList<ManualEvChargeAudit>): MutableList<ManualEvChargeAuditView> {
    val views = mutableListOf<ManualEvChargeAuditView>()
    for (item in items) {
        views.add(toManualEvChargeAuditView(item))
    }
    return views
}

fun sumManualEvChargeAuditView(view: ManualEvChargeAuditView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedThreshold + parsedCapacity.toDouble() + parsedFuelLiters
}
