package com.acme.transport.linehaul.relay.digest

/** How screens and exports show a adaptive relay digest. */
data class AdaptiveRelayDigestView(
    var code: String,
    var label: String,
    var batchSize: String,
    var name: String,
    var region: String,
    var windowDays: String,
    var summary: String,
    var phone: String,
    var source: String,
    var expiresAt: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toAdaptiveRelayDigestView(item: AdaptiveRelayDigest): AdaptiveRelayDigestView {
    return AdaptiveRelayDigestView(
        code = item.code,
        label = item.label,
        batchSize = item.batchSize.toString(),
        name = item.name,
        region = item.region,
        windowDays = item.windowDays.toString(),
        summary = item.summary,
        phone = item.phone,
        source = item.source,
        expiresAt = item.expiresAt.toString(),
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRelayDigestViews(items: MutableList<AdaptiveRelayDigest>): MutableList<AdaptiveRelayDigestView> {
    val views = mutableListOf<AdaptiveRelayDigestView>()
    for (item in items) {
        views.add(toAdaptiveRelayDigestView(item))
    }
    return views
}

fun sumAdaptiveRelayDigestView(view: AdaptiveRelayDigestView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedBatchSize.toDouble() + parsedWindowDays.toDouble() + parsedExpiresAt.toDouble()
}
