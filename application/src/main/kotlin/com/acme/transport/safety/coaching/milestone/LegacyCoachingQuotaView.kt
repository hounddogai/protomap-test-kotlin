package com.acme.transport.safety.coaching.milestone

/** How screens and exports show a legacy coaching quota. */
data class LegacyCoachingQuotaView(
    var code: String,
    var label: String,
    var limit: String,
    var region: String,
    var weight: String,
    var versionText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toLegacyCoachingQuotaView(item: LegacyCoachingQuota): LegacyCoachingQuotaView {
    return LegacyCoachingQuotaView(
        code = item.code,
        label = item.label,
        limit = item.limit.toString(),
        region = item.region,
        weight = "%.2f".format(item.weight),
        versionText = item.version.toString(),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCoachingQuotaViews(items: MutableList<LegacyCoachingQuota>): MutableList<LegacyCoachingQuotaView> {
    val views = mutableListOf<LegacyCoachingQuotaView>()
    for (item in items) {
        views.add(toLegacyCoachingQuotaView(item))
    }
    return views
}

fun sumLegacyCoachingQuotaView(view: LegacyCoachingQuotaView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedLimit.toDouble() + parsedWeight + parsedVersion.toDouble()
}
