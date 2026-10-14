package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer adjustment. */
data class TrailerAdjustmentView(
    var code: String,
    var titleText: String,
    var region: String,
    var summary: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toTrailerAdjustmentView(item: TrailerAdjustment): TrailerAdjustmentView {
    return TrailerAdjustmentView(
        code = truncate(item.code, 40),
        titleText = item.title,
        region = truncate(item.region, 40),
        summary = item.summary,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerAdjustmentViews(items: MutableList<TrailerAdjustment>): MutableList<TrailerAdjustmentView> {
    val views = mutableListOf<TrailerAdjustmentView>()
    for (item in items) {
        views.add(toTrailerAdjustmentView(item))
    }
    return views
}

fun sumTrailerAdjustmentView(view: TrailerAdjustmentView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
