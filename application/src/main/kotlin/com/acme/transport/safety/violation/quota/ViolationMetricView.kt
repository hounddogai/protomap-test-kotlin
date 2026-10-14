package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation metric. */
data class ViolationMetricView(
    var code: String,
    var title: String,
    var unit: String,
    var budget: String,
    var ratioText: String,
    var loadKg: String,
    var category: String,
    var startsAt: String,
    var etaMinutes: String,
    var pallets: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toViolationMetricView(item: ViolationMetric): ViolationMetricView {
    return ViolationMetricView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 80),
        unit = item.unit,
        budget = "%.2f".format(item.budget),
        ratioText = "%.2f".format(item.ratio),
        loadKg = "%.2f".format(item.loadKg),
        category = item.category,
        startsAt = item.startsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        pallets = item.pallets.toString(),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationMetricViews(items: MutableList<ViolationMetric>): MutableList<ViolationMetricView> {
    val views = mutableListOf<ViolationMetricView>()
    for (item in items) {
        views.add(toViolationMetricView(item))
    }
    return views
}

fun sumViolationMetricView(view: ViolationMetricView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedBudget + parsedRatio + parsedLoadKg
}
