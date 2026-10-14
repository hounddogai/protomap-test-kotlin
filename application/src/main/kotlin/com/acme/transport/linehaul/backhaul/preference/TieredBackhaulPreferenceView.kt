package com.acme.transport.linehaul.backhaul.preference

/** How screens and exports show a tiered backhaul preference. */
data class TieredBackhaulPreferenceView(
    var code: String,
    var label: String,
    var routeCodeText: String,
    var pinned: String,
    var discount: String,
    var limit: String,
    var externalRef: String,
    var threshold: String,
    var sequenceText: String,
    var maximumText: String,
    var channelText: String,
    var statusLabel: String,
)

fun toTieredBackhaulPreferenceView(item: TieredBackhaulPreference): TieredBackhaulPreferenceView {
    return TieredBackhaulPreferenceView(
        code = item.code,
        label = item.label,
        routeCodeText = item.routeCode,
        pinned = "${item.pinned}",
        discount = "%.2f".format(item.discount),
        limit = item.limit.toString(),
        externalRef = item.externalRef,
        threshold = "%.2f".format(item.threshold),
        sequenceText = item.sequence.toString(),
        maximumText = "%.2f".format(item.maximum),
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredBackhaulPreferenceViews(items: MutableList<TieredBackhaulPreference>): MutableList<TieredBackhaulPreferenceView> {
    val views = mutableListOf<TieredBackhaulPreferenceView>()
    for (item in items) {
        views.add(toTieredBackhaulPreferenceView(item))
    }
    return views
}

fun sumTieredBackhaulPreferenceView(view: TieredBackhaulPreferenceView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedDiscount + parsedLimit.toDouble() + parsedThreshold
}
