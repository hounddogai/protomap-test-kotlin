package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return metric. */
data class LeaseReturnMetricView(
    var code: String,
    var title: String,
    var region: String,
    var unit: String,
    var categoryText: String,
    var externalRef: String,
    var priority: String,
    var statusLabel: String,
)

fun toLeaseReturnMetricView(item: LeaseReturnMetric): LeaseReturnMetricView {
    return LeaseReturnMetricView(
        code = truncate(item.code, 80),
        title = item.title,
        region = item.region,
        unit = item.unit,
        categoryText = truncate(item.category, 120),
        externalRef = item.externalRef,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnMetricViews(items: MutableList<LeaseReturnMetric>): MutableList<LeaseReturnMetricView> {
    val views = mutableListOf<LeaseReturnMetricView>()
    for (item in items) {
        views.add(toLeaseReturnMetricView(item))
    }
    return views
}

fun sumLeaseReturnMetricView(view: LeaseReturnMetricView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
