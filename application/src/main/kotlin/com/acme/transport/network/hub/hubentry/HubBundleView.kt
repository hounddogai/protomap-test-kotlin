package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub bundle. */
data class HubBundleView(
    var code: String,
    var label: String,
    var depotText: String,
    var channel: String,
    var descriptionText: String,
    var timezone: String,
    var amount: String,
    var stopCount: String,
    var phone: String,
    var country: String,
    var statusLabel: String,
)

fun toHubBundleView(item: HubBundle): HubBundleView {
    return HubBundleView(
        code = item.code,
        label = item.label,
        depotText = item.depot,
        channel = item.channel,
        descriptionText = item.description,
        timezone = truncate(item.timezone, 80),
        amount = "%.2f".format(item.amount),
        stopCount = item.stopCount.toString(),
        phone = item.phone,
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubBundleViews(items: MutableList<HubBundle>): MutableList<HubBundleView> {
    val views = mutableListOf<HubBundleView>()
    for (item in items) {
        views.add(toHubBundleView(item))
    }
    return views
}

fun sumHubBundleView(view: HubBundleView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedAmount + parsedStopCount.toDouble()
}
