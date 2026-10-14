package com.acme.transport.safety.scorecard.milestone

/** How screens and exports show a scorecard plan. */
data class ScorecardPlanView(
    var code: String,
    var label: String,
    var maximum: String,
    var summary: String,
    var quantityText: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toScorecardPlanView(item: ScorecardPlan): ScorecardPlanView {
    return ScorecardPlanView(
        code = item.code,
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        summary = item.summary,
        quantityText = item.quantity.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardPlanViews(items: MutableList<ScorecardPlan>): MutableList<ScorecardPlanView> {
    val views = mutableListOf<ScorecardPlanView>()
    for (item in items) {
        views.add(toScorecardPlanView(item))
    }
    return views
}

fun sumScorecardPlanView(view: ScorecardPlanView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedMaximum + parsedQuantity.toDouble()
}
