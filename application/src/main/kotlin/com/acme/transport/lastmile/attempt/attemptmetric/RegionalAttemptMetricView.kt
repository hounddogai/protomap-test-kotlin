package com.acme.transport.lastmile.attempt.attemptmetric

/** How screens and exports show a regional attempt metric. */
data class RegionalAttemptMetricView(
    var code: String,
    var title: String,
    var pinnedText: String,
    var archived: String,
    var locale: String,
    var cost: String,
    var unit: String,
    var discount: String,
    var statusLabel: String,
)

fun toRegionalAttemptMetricView(item: RegionalAttemptMetric): RegionalAttemptMetricView {
    return RegionalAttemptMetricView(
        code = item.code,
        title = item.title,
        pinnedText = "${item.pinned}",
        archived = "${item.archived}",
        locale = item.locale,
        cost = "%.2f".format(item.cost),
        unit = item.unit,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAttemptMetricViews(items: MutableList<RegionalAttemptMetric>): MutableList<RegionalAttemptMetricView> {
    val views = mutableListOf<RegionalAttemptMetricView>()
    for (item in items) {
        views.add(toRegionalAttemptMetricView(item))
    }
    return views
}

fun sumRegionalAttemptMetricView(view: RegionalAttemptMetricView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedCost + parsedDiscount
}
