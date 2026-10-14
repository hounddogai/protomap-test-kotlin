package com.acme.transport.safety.nearmiss.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss target. */
data class NearMissTargetView(
    var code: String,
    var label: String,
    var lane: String,
    var approved: String,
    var country: String,
    var windowDays: String,
    var region: String,
    var attempts: String,
    var maximum: String,
    var statusLabel: String,
)

fun toNearMissTargetView(item: NearMissTarget): NearMissTargetView {
    return NearMissTargetView(
        code = item.code,
        label = item.label,
        lane = item.lane,
        approved = "${item.approved}",
        country = truncate(item.country, 64),
        windowDays = item.windowDays.toString(),
        region = item.region,
        attempts = item.attempts.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissTargetViews(items: MutableList<NearMissTarget>): MutableList<NearMissTargetView> {
    val views = mutableListOf<NearMissTargetView>()
    for (item in items) {
        views.add(toNearMissTargetView(item))
    }
    return views
}

fun sumNearMissTargetView(view: NearMissTargetView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedWindowDays.toDouble() + parsedAttempts.toDouble() + parsedMaximum
}
