package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone milestone. */
data class ZoneMilestoneView(
    var code: String,
    var titleText: String,
    var channel: String,
    var currency: String,
    var contactEmail: String,
    var trailerId: String,
    var region: String,
    var rate: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toZoneMilestoneView(item: ZoneMilestone): ZoneMilestoneView {
    return ZoneMilestoneView(
        code = item.code,
        titleText = truncate(item.title, 120),
        channel = item.channel,
        currency = truncate(item.currency, 64),
        contactEmail = item.contactEmail,
        trailerId = truncate(item.trailerId, 80),
        region = item.region,
        rate = "%.2f".format(item.rate),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneMilestoneViews(items: MutableList<ZoneMilestone>): MutableList<ZoneMilestoneView> {
    val views = mutableListOf<ZoneMilestoneView>()
    for (item in items) {
        views.add(toZoneMilestoneView(item))
    }
    return views
}

fun sumZoneMilestoneView(view: ZoneMilestoneView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
