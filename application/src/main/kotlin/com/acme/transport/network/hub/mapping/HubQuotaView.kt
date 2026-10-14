package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub quota. */
data class HubQuotaView(
    var codeText: String,
    var name: String,
    var active: String,
    var timezone: String,
    var endsAtText: String,
    var expiresAt: String,
    var checksum: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toHubQuotaView(item: HubQuota): HubQuotaView {
    return HubQuotaView(
        codeText = item.code,
        name = truncate(item.name, 40),
        active = "${item.active}",
        timezone = truncate(item.timezone, 120),
        endsAtText = item.endsAt.toString(),
        expiresAt = item.expiresAt.toString(),
        checksum = item.checksum,
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubQuotaViews(items: MutableList<HubQuota>): MutableList<HubQuotaView> {
    val views = mutableListOf<HubQuotaView>()
    for (item in items) {
        views.add(toHubQuotaView(item))
    }
    return views
}

fun sumHubQuotaView(view: HubQuotaView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedEndsAt.toDouble() + parsedExpiresAt.toDouble() + parsedGraceDays.toDouble()
}
