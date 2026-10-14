package com.acme.transport.network.transittime.preference

/** How screens and exports show a strict transit time baseline. */
data class StrictTransitTimeBaselineView(
    var code: String,
    var label: String,
    var limit: String,
    var priority: String,
    var channel: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toStrictTransitTimeBaselineView(item: StrictTransitTimeBaseline): StrictTransitTimeBaselineView {
    return StrictTransitTimeBaselineView(
        code = item.code,
        label = item.label,
        limit = item.limit.toString(),
        priority = item.priority.toString(),
        channel = item.channel,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTransitTimeBaselineViews(items: MutableList<StrictTransitTimeBaseline>): MutableList<StrictTransitTimeBaselineView> {
    val views = mutableListOf<StrictTransitTimeBaselineView>()
    for (item in items) {
        views.add(toStrictTransitTimeBaselineView(item))
    }
    return views
}

fun sumStrictTransitTimeBaselineView(view: StrictTransitTimeBaselineView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedLimit.toDouble() + parsedPriority.toDouble()
}
