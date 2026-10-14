package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer metric. */
data class ScheduledTrailerMetricView(
    var code: String,
    var title: String,
    var sourceText: String,
    var discount: String,
    var priority: String,
    var name: String,
    var statusLabel: String,
)

fun toScheduledTrailerMetricView(item: ScheduledTrailerMetric): ScheduledTrailerMetricView {
    return ScheduledTrailerMetricView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 40),
        sourceText = item.source,
        discount = "%.2f".format(item.discount),
        priority = item.priority.toString(),
        name = truncate(item.name, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerMetricViews(items: MutableList<ScheduledTrailerMetric>): MutableList<ScheduledTrailerMetricView> {
    val views = mutableListOf<ScheduledTrailerMetricView>()
    for (item in items) {
        views.add(toScheduledTrailerMetricView(item))
    }
    return views
}

fun sumScheduledTrailerMetricView(view: ScheduledTrailerMetricView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedDiscount + parsedPriority.toDouble()
}
