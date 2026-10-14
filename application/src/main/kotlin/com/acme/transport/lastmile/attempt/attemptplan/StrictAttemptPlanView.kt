package com.acme.transport.lastmile.attempt.attemptplan

/** How screens and exports show a strict attempt plan. */
data class StrictAttemptPlanView(
    var code: String,
    var title: String,
    var depot: String,
    var visibleText: String,
    var currencyText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toStrictAttemptPlanView(item: StrictAttemptPlan): StrictAttemptPlanView {
    return StrictAttemptPlanView(
        code = item.code,
        title = item.title,
        depot = item.depot,
        visibleText = "${item.visible}",
        currencyText = item.currency,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAttemptPlanViews(items: MutableList<StrictAttemptPlan>): MutableList<StrictAttemptPlanView> {
    val views = mutableListOf<StrictAttemptPlanView>()
    for (item in items) {
        views.add(toStrictAttemptPlanView(item))
    }
    return views
}

fun sumStrictAttemptPlanView(view: StrictAttemptPlanView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
