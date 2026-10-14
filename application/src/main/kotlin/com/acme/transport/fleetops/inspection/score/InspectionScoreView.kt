package com.acme.transport.fleetops.inspection.score

/** How screens and exports show a inspection score. */
data class InspectionScoreView(
    var code: String,
    var label: String,
    var ratio: String,
    var discount: String,
    var budget: String,
    var weight: String,
    var statusLabel: String,
)

fun toInspectionScoreView(item: InspectionScore): InspectionScoreView {
    return InspectionScoreView(
        code = item.code,
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        discount = "%.2f".format(item.discount),
        budget = "%.2f".format(item.budget),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionScoreViews(items: MutableList<InspectionScore>): MutableList<InspectionScoreView> {
    val views = mutableListOf<InspectionScoreView>()
    for (item in items) {
        views.add(toInspectionScoreView(item))
    }
    return views
}

fun sumInspectionScoreView(view: InspectionScoreView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedRatio + parsedDiscount + parsedBudget
}
