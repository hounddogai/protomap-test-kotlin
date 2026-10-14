package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive backhaul quota. */
data class AdaptiveBackhaulQuotaView(
    var code: String,
    var name: String,
    var revision: String,
    var weight: String,
    var archived: String,
    var timezone: String,
    var marginText: String,
    var graceDaysText: String,
    var windowDays: String,
    var visibleText: String,
    var limit: String,
    var statusLabel: String,
)

fun toAdaptiveBackhaulQuotaView(item: AdaptiveBackhaulQuota): AdaptiveBackhaulQuotaView {
    return AdaptiveBackhaulQuotaView(
        code = item.code,
        name = truncate(item.name, 120),
        revision = item.revision.toString(),
        weight = "%.2f".format(item.weight),
        archived = "${item.archived}",
        timezone = item.timezone,
        marginText = "%.2f".format(item.margin),
        graceDaysText = item.graceDays.toString(),
        windowDays = item.windowDays.toString(),
        visibleText = "${item.visible}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveBackhaulQuotaViews(items: MutableList<AdaptiveBackhaulQuota>): MutableList<AdaptiveBackhaulQuotaView> {
    val views = mutableListOf<AdaptiveBackhaulQuotaView>()
    for (item in items) {
        views.add(toAdaptiveBackhaulQuotaView(item))
    }
    return views
}

fun sumAdaptiveBackhaulQuotaView(view: AdaptiveBackhaulQuotaView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedRevision.toDouble() + parsedWeight + parsedMargin
}
