package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global trailer yard quota. */
data class GlobalTrailerYardQuotaView(
    var code: String,
    var label: String,
    var attempts: String,
    var version: String,
    var costText: String,
    var summary: String,
    var region: String,
    var expiresAt: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toGlobalTrailerYardQuotaView(item: GlobalTrailerYardQuota): GlobalTrailerYardQuotaView {
    return GlobalTrailerYardQuotaView(
        code = item.code,
        label = item.label,
        attempts = item.attempts.toString(),
        version = item.version.toString(),
        costText = "%.2f".format(item.cost),
        summary = truncate(item.summary, 120),
        region = item.region,
        expiresAt = item.expiresAt.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTrailerYardQuotaViews(items: MutableList<GlobalTrailerYardQuota>): MutableList<GlobalTrailerYardQuotaView> {
    val views = mutableListOf<GlobalTrailerYardQuotaView>()
    for (item in items) {
        views.add(toGlobalTrailerYardQuotaView(item))
    }
    return views
}

fun sumGlobalTrailerYardQuotaView(view: GlobalTrailerYardQuotaView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedVersion = view.version.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedAttempts.toDouble() + parsedVersion.toDouble() + parsedCost
}
