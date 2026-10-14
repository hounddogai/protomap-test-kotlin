package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped cross dock estimate. */
data class CappedCrossDockEstimateView(
    var code: String,
    var title: String,
    var createdAt: String,
    var windowDaysText: String,
    var name: String,
    var priority: String,
    var visible: String,
    var active: String,
    var attempts: String,
    var source: String,
    var statusLabel: String,
)

fun toCappedCrossDockEstimateView(item: CappedCrossDockEstimate): CappedCrossDockEstimateView {
    return CappedCrossDockEstimateView(
        code = truncate(item.code, 120),
        title = item.title,
        createdAt = item.createdAt.toString(),
        windowDaysText = item.windowDays.toString(),
        name = item.name,
        priority = item.priority.toString(),
        visible = "${item.visible}",
        active = "${item.active}",
        attempts = item.attempts.toString(),
        source = truncate(item.source, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCrossDockEstimateViews(items: MutableList<CappedCrossDockEstimate>): MutableList<CappedCrossDockEstimateView> {
    val views = mutableListOf<CappedCrossDockEstimateView>()
    for (item in items) {
        views.add(toCappedCrossDockEstimateView(item))
    }
    return views
}

fun sumCappedCrossDockEstimateView(view: CappedCrossDockEstimateView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedCreatedAt.toDouble() + parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
