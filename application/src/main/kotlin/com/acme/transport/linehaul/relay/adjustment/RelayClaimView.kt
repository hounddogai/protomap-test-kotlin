package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay claim. */
data class RelayClaimView(
    var code: String,
    var title: String,
    var category: String,
    var maximum: String,
    var labelText: String,
    var loadKg: String,
    var retries: String,
    var driverIdText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toRelayClaimView(item: RelayClaim): RelayClaimView {
    return RelayClaimView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 120),
        category = item.category,
        maximum = "%.2f".format(item.maximum),
        labelText = item.label,
        loadKg = "%.2f".format(item.loadKg),
        retries = item.retries.toString(),
        driverIdText = truncate(item.driverId, 64),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayClaimViews(items: MutableList<RelayClaim>): MutableList<RelayClaimView> {
    val views = mutableListOf<RelayClaimView>()
    for (item in items) {
        views.add(toRelayClaimView(item))
    }
    return views
}

fun sumRelayClaimView(view: RelayClaimView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedMaximum + parsedLoadKg + parsedRetries.toDouble()
}
