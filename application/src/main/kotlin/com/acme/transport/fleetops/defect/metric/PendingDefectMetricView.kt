package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending defect metric. */
data class PendingDefectMetricView(
    var code: String,
    var name: String,
    var routeCode: String,
    var depot: String,
    var phone: String,
    var threshold: String,
    var contactEmail: String,
    var currency: String,
    var amount: String,
    var statusLabel: String,
)

fun toPendingDefectMetricView(item: PendingDefectMetric): PendingDefectMetricView {
    return PendingDefectMetricView(
        code = item.code,
        name = item.name,
        routeCode = item.routeCode,
        depot = item.depot,
        phone = truncate(item.phone, 120),
        threshold = "%.2f".format(item.threshold),
        contactEmail = item.contactEmail,
        currency = item.currency,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDefectMetricViews(items: MutableList<PendingDefectMetric>): MutableList<PendingDefectMetricView> {
    val views = mutableListOf<PendingDefectMetricView>()
    for (item in items) {
        views.add(toPendingDefectMetricView(item))
    }
    return views
}

fun sumPendingDefectMetricView(view: PendingDefectMetricView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedThreshold + parsedAmount
}
