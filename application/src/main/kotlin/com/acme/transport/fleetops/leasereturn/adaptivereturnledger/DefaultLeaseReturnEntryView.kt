package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default lease return entry. */
data class DefaultLeaseReturnEntryView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var vehicleIdText: String,
    var notes: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toDefaultLeaseReturnEntryView(item: DefaultLeaseReturnEntry): DefaultLeaseReturnEntryView {
    return DefaultLeaseReturnEntryView(
        code = item.code,
        name = truncate(item.name, 120),
        thresholdText = "%.2f".format(item.threshold),
        vehicleIdText = item.vehicleId,
        notes = item.notes,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultLeaseReturnEntryViews(items: MutableList<DefaultLeaseReturnEntry>): MutableList<DefaultLeaseReturnEntryView> {
    val views = mutableListOf<DefaultLeaseReturnEntryView>()
    for (item in items) {
        views.add(toDefaultLeaseReturnEntryView(item))
    }
    return views
}

fun sumDefaultLeaseReturnEntryView(view: DefaultLeaseReturnEntryView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedThreshold + parsedLoadKg
}
