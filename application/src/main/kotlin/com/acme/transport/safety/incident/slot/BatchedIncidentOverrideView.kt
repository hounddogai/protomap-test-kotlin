package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched incident override. */
data class BatchedIncidentOverrideView(
    var codeText: String,
    var label: String,
    var dockText: String,
    var pinned: String,
    var currencyText: String,
    var checksum: String,
    var routeCode: String,
    var expiresAt: String,
    var timezoneText: String,
    var minimum: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toBatchedIncidentOverrideView(item: BatchedIncidentOverride): BatchedIncidentOverrideView {
    return BatchedIncidentOverrideView(
        codeText = item.code,
        label = item.label,
        dockText = item.dock,
        pinned = "${item.pinned}",
        currencyText = item.currency,
        checksum = item.checksum,
        routeCode = truncate(item.routeCode, 64),
        expiresAt = item.expiresAt.toString(),
        timezoneText = truncate(item.timezone, 80),
        minimum = "%.2f".format(item.minimum),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedIncidentOverrideViews(items: MutableList<BatchedIncidentOverride>): MutableList<BatchedIncidentOverrideView> {
    val views = mutableListOf<BatchedIncidentOverrideView>()
    for (item in items) {
        views.add(toBatchedIncidentOverrideView(item))
    }
    return views
}

fun sumBatchedIncidentOverrideView(view: BatchedIncidentOverrideView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedExpiresAt.toDouble() + parsedMinimum + parsedPallets.toDouble()
}
