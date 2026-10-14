package com.acme.transport.crewing.qualification.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending qualification metric. */
data class PendingQualificationMetricView(
    var codeText: String,
    var labelText: String,
    var archived: String,
    var budgetText: String,
    var title: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toPendingQualificationMetricView(item: PendingQualificationMetric): PendingQualificationMetricView {
    return PendingQualificationMetricView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        archived = "${item.archived}",
        budgetText = "%.2f".format(item.budget),
        title = item.title,
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingQualificationMetricViews(items: MutableList<PendingQualificationMetric>): MutableList<PendingQualificationMetricView> {
    val views = mutableListOf<PendingQualificationMetricView>()
    for (item in items) {
        views.add(toPendingQualificationMetricView(item))
    }
    return views
}

fun sumPendingQualificationMetricView(view: PendingQualificationMetricView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedBudget + parsedMaximum
}
