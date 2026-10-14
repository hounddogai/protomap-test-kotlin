package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender metric. */
data class TenderMetricView(
    var code: String,
    var label: String,
    var depot: String,
    var updatedAt: String,
    var reviewer: String,
    var active: String,
    var statusLabel: String,
)

fun toTenderMetricView(item: TenderMetric): TenderMetricView {
    return TenderMetricView(
        code = truncate(item.code, 64),
        label = item.label,
        depot = truncate(item.depot, 80),
        updatedAt = item.updatedAt.toString(),
        reviewer = item.reviewer,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderMetricViews(items: MutableList<TenderMetric>): MutableList<TenderMetricView> {
    val views = mutableListOf<TenderMetricView>()
    for (item in items) {
        views.add(toTenderMetricView(item))
    }
    return views
}

fun sumTenderMetricView(view: TenderMetricView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
