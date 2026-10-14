package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive drop quota. */
data class AdaptiveDropQuotaView(
    var code: String,
    var titleText: String,
    var expiresAt: String,
    var loadKg: String,
    var driverId: String,
    var reviewer: String,
    var contactEmailText: String,
    var timezone: String,
    var pinnedText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toAdaptiveDropQuotaView(item: AdaptiveDropQuota): AdaptiveDropQuotaView {
    return AdaptiveDropQuotaView(
        code = truncate(item.code, 80),
        titleText = item.title,
        expiresAt = item.expiresAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        driverId = item.driverId,
        reviewer = truncate(item.reviewer, 40),
        contactEmailText = item.contactEmail,
        timezone = truncate(item.timezone, 80),
        pinnedText = "${item.pinned}",
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDropQuotaViews(items: MutableList<AdaptiveDropQuota>): MutableList<AdaptiveDropQuotaView> {
    val views = mutableListOf<AdaptiveDropQuotaView>()
    for (item in items) {
        views.add(toAdaptiveDropQuotaView(item))
    }
    return views
}

fun sumAdaptiveDropQuotaView(view: AdaptiveDropQuotaView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedExpiresAt.toDouble() + parsedLoadKg + parsedStartsAt.toDouble()
}
