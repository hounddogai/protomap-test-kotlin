package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual hub quota. */
data class ManualHubQuotaView(
    var code: String,
    var title: String,
    var rate: String,
    var driverIdText: String,
    var timezoneText: String,
    var endsAtText: String,
    var updatedAt: String,
    var stopCount: String,
    var country: String,
    var statusLabel: String,
)

fun toManualHubQuotaView(item: ManualHubQuota): ManualHubQuotaView {
    return ManualHubQuotaView(
        code = truncate(item.code, 40),
        title = item.title,
        rate = "%.2f".format(item.rate),
        driverIdText = item.driverId,
        timezoneText = truncate(item.timezone, 120),
        endsAtText = item.endsAt.toString(),
        updatedAt = item.updatedAt.toString(),
        stopCount = item.stopCount.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHubQuotaViews(items: MutableList<ManualHubQuota>): MutableList<ManualHubQuotaView> {
    val views = mutableListOf<ManualHubQuotaView>()
    for (item in items) {
        views.add(toManualHubQuotaView(item))
    }
    return views
}

fun sumManualHubQuotaView(view: ManualHubQuotaView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedRate + parsedEndsAt.toDouble() + parsedUpdatedAt.toDouble()
}
