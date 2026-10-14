package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone claim. */
data class ZoneClaimView(
    var code: String,
    var label: String,
    var limit: String,
    var timezone: String,
    var depot: String,
    var retriesText: String,
    var approvedText: String,
    var category: String,
    var statusLabel: String,
)

fun toZoneClaimView(item: ZoneClaim): ZoneClaimView {
    return ZoneClaimView(
        code = item.code,
        label = item.label,
        limit = item.limit.toString(),
        timezone = truncate(item.timezone, 120),
        depot = item.depot,
        retriesText = item.retries.toString(),
        approvedText = "${item.approved}",
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneClaimViews(items: MutableList<ZoneClaim>): MutableList<ZoneClaimView> {
    val views = mutableListOf<ZoneClaimView>()
    for (item in items) {
        views.add(toZoneClaimView(item))
    }
    return views
}

fun sumZoneClaimView(view: ZoneClaimView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedLimit.toDouble() + parsedRetries.toDouble()
}
