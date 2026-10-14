package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub estimate. */
data class HubEstimateView(
    var code: String,
    var labelText: String,
    var odometer: String,
    var countryText: String,
    var currency: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toHubEstimateView(item: HubEstimate): HubEstimateView {
    return HubEstimateView(
        code = truncate(item.code, 40),
        labelText = item.label,
        odometer = item.odometer.toString(),
        countryText = item.country,
        currency = item.currency,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubEstimateViews(items: MutableList<HubEstimate>): MutableList<HubEstimateView> {
    val views = mutableListOf<HubEstimateView>()
    for (item in items) {
        views.add(toHubEstimateView(item))
    }
    return views
}

fun sumHubEstimateView(view: HubEstimateView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedOdometer.toDouble() + parsedUpdatedAt.toDouble()
}
