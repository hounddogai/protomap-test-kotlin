package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict backhaul baseline. */
data class StrictBackhaulBaselineView(
    var code: String,
    var label: String,
    var durationMinutes: String,
    var externalRef: String,
    var expiresAt: String,
    var rate: String,
    var statusLabel: String,
)

fun toStrictBackhaulBaselineView(item: StrictBackhaulBaseline): StrictBackhaulBaselineView {
    return StrictBackhaulBaselineView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 40),
        durationMinutes = item.durationMinutes.toString(),
        externalRef = truncate(item.externalRef, 40),
        expiresAt = item.expiresAt.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBackhaulBaselineViews(items: MutableList<StrictBackhaulBaseline>): MutableList<StrictBackhaulBaselineView> {
    val views = mutableListOf<StrictBackhaulBaselineView>()
    for (item in items) {
        views.add(toStrictBackhaulBaselineView(item))
    }
    return views
}

fun sumStrictBackhaulBaselineView(view: StrictBackhaulBaselineView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedDurationMinutes.toDouble() + parsedExpiresAt.toDouble() + parsedRate
}
