package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tire quota. */
data class DefaultTireQuotaView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var segmentText: String,
    var retries: String,
    var ownerIdText: String,
    var timezoneText: String,
    var approved: String,
    var ratioText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toDefaultTireQuotaView(item: DefaultTireQuota): DefaultTireQuotaView {
    return DefaultTireQuotaView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        segmentText = item.segment,
        retries = item.retries.toString(),
        ownerIdText = truncate(item.ownerId, 120),
        timezoneText = item.timezone,
        approved = "${item.approved}",
        ratioText = "%.2f".format(item.ratio),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTireQuotaViews(items: MutableList<DefaultTireQuota>): MutableList<DefaultTireQuotaView> {
    val views = mutableListOf<DefaultTireQuotaView>()
    for (item in items) {
        views.add(toDefaultTireQuotaView(item))
    }
    return views
}

fun sumDefaultTireQuotaView(view: DefaultTireQuotaView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRetries.toDouble() + parsedRatio
}
