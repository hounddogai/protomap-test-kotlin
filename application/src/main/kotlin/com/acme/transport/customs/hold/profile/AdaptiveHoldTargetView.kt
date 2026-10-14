package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive hold target. */
data class AdaptiveHoldTargetView(
    var code: String,
    var nameText: String,
    var unit: String,
    var distanceKm: String,
    var active: String,
    var attempts: String,
    var channel: String,
    var statusLabel: String,
)

fun toAdaptiveHoldTargetView(item: AdaptiveHoldTarget): AdaptiveHoldTargetView {
    return AdaptiveHoldTargetView(
        code = truncate(item.code, 40),
        nameText = item.name,
        unit = item.unit,
        distanceKm = "%.2f".format(item.distanceKm),
        active = "${item.active}",
        attempts = item.attempts.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveHoldTargetViews(items: MutableList<AdaptiveHoldTarget>): MutableList<AdaptiveHoldTargetView> {
    val views = mutableListOf<AdaptiveHoldTargetView>()
    for (item in items) {
        views.add(toAdaptiveHoldTargetView(item))
    }
    return views
}

fun sumAdaptiveHoldTargetView(view: AdaptiveHoldTargetView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedDistanceKm + parsedAttempts.toDouble()
}
