package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

/** How screens and exports show a primary backhaul metric. */
data class PrimaryBackhaulMetricView(
    var code: String,
    var label: String,
    var amount: String,
    var limit: String,
    var createdAt: String,
    var revision: String,
    var statusLabel: String,
)

fun toPrimaryBackhaulMetricView(item: PrimaryBackhaulMetric): PrimaryBackhaulMetricView {
    return PrimaryBackhaulMetricView(
        code = item.code,
        label = item.label,
        amount = "%.2f".format(item.amount),
        limit = item.limit.toString(),
        createdAt = item.createdAt.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryBackhaulMetricViews(items: MutableList<PrimaryBackhaulMetric>): MutableList<PrimaryBackhaulMetricView> {
    val views = mutableListOf<PrimaryBackhaulMetricView>()
    for (item in items) {
        views.add(toPrimaryBackhaulMetricView(item))
    }
    return views
}

fun sumPrimaryBackhaulMetricView(view: PrimaryBackhaulMetricView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedAmount + parsedLimit.toDouble() + parsedCreatedAt.toDouble()
}
