package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual trailer variance. */
data class ManualTrailerVarianceView(
    var codeText: String,
    var name: String,
    var priorityText: String,
    var archivedText: String,
    var quantityText: String,
    var budget: String,
    var statusLabel: String,
)

fun toManualTrailerVarianceView(item: ManualTrailerVariance): ManualTrailerVarianceView {
    return ManualTrailerVarianceView(
        codeText = item.code,
        name = truncate(item.name, 120),
        priorityText = item.priority.toString(),
        archivedText = "${item.archived}",
        quantityText = item.quantity.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerVarianceViews(items: MutableList<ManualTrailerVariance>): MutableList<ManualTrailerVarianceView> {
    val views = mutableListOf<ManualTrailerVarianceView>()
    for (item in items) {
        views.add(toManualTrailerVarianceView(item))
    }
    return views
}

fun sumManualTrailerVarianceView(view: ManualTrailerVarianceView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedPriority.toDouble() + parsedQuantity.toDouble() + parsedBudget
}
