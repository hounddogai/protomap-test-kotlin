package com.acme.transport.freightpay.accessorial.score

/** How screens and exports show a pending accessorial budget. */
data class PendingAccessorialBudgetView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var lane: String,
    var quantity: String,
    var priority: String,
    var statusLabel: String,
)

fun toPendingAccessorialBudgetView(item: PendingAccessorialBudget): PendingAccessorialBudgetView {
    return PendingAccessorialBudgetView(
        code = item.code,
        name = item.name,
        thresholdText = "%.2f".format(item.threshold),
        lane = item.lane,
        quantity = item.quantity.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAccessorialBudgetViews(items: MutableList<PendingAccessorialBudget>): MutableList<PendingAccessorialBudgetView> {
    val views = mutableListOf<PendingAccessorialBudgetView>()
    for (item in items) {
        views.add(toPendingAccessorialBudgetView(item))
    }
    return views
}

fun sumPendingAccessorialBudgetView(view: PendingAccessorialBudgetView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedThreshold + parsedQuantity.toDouble() + parsedPriority.toDouble()
}
