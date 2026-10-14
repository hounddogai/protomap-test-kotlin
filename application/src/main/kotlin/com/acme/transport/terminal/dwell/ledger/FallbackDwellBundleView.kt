package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dwell bundle. */
data class FallbackDwellBundleView(
    var code: String,
    var label: String,
    var segment: String,
    var score: String,
    var graceDays: String,
    var carrier: String,
    var timezoneText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toFallbackDwellBundleView(item: FallbackDwellBundle): FallbackDwellBundleView {
    return FallbackDwellBundleView(
        code = item.code,
        label = item.label,
        segment = item.segment,
        score = "%.2f".format(item.score),
        graceDays = item.graceDays.toString(),
        carrier = item.carrier,
        timezoneText = truncate(item.timezone, 40),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDwellBundleViews(items: MutableList<FallbackDwellBundle>): MutableList<FallbackDwellBundleView> {
    val views = mutableListOf<FallbackDwellBundleView>()
    for (item in items) {
        views.add(toFallbackDwellBundleView(item))
    }
    return views
}

fun sumFallbackDwellBundleView(view: FallbackDwellBundleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedScore + parsedGraceDays.toDouble() + parsedCreatedAt.toDouble()
}
