package com.acme.transport.freightpay.accessorial.manifest

/** How screens and exports show a accessorial budget. */
data class AccessorialBudgetView(
    var code: String,
    var title: String,
    var currency: String,
    var floorValue: String,
    var etaMinutes: String,
    var thresholdText: String,
    var cost: String,
    var attempts: String,
    var statusLabel: String,
)

fun toAccessorialBudgetView(item: AccessorialBudget): AccessorialBudgetView {
    return AccessorialBudgetView(
        code = item.code,
        title = item.title,
        currency = item.currency,
        floorValue = "%.2f".format(item.floorValue),
        etaMinutes = item.etaMinutes.toString(),
        thresholdText = "%.2f".format(item.threshold),
        cost = "%.2f".format(item.cost),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialBudgetViews(items: MutableList<AccessorialBudget>): MutableList<AccessorialBudgetView> {
    val views = mutableListOf<AccessorialBudgetView>()
    for (item in items) {
        views.add(toAccessorialBudgetView(item))
    }
    return views
}

fun sumAccessorialBudgetView(view: AccessorialBudgetView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedFloorValue + parsedEtaMinutes.toDouble() + parsedThreshold
}
