package com.acme.transport.freightpay.accessorial.accessorialbudget

/** How screens and exports show a manual accessorial budget. */
data class ManualAccessorialBudgetView(
    var code: String,
    var label: String,
    var thresholdText: String,
    var createdAt: String,
    var graceDays: String,
    var driverId: String,
    var statusLabel: String,
)

fun toManualAccessorialBudgetView(item: ManualAccessorialBudget): ManualAccessorialBudgetView {
    return ManualAccessorialBudgetView(
        code = item.code,
        label = item.label,
        thresholdText = "%.2f".format(item.threshold),
        createdAt = item.createdAt.toString(),
        graceDays = item.graceDays.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAccessorialBudgetViews(items: MutableList<ManualAccessorialBudget>): MutableList<ManualAccessorialBudgetView> {
    val views = mutableListOf<ManualAccessorialBudgetView>()
    for (item in items) {
        views.add(toManualAccessorialBudgetView(item))
    }
    return views
}

fun sumManualAccessorialBudgetView(view: ManualAccessorialBudgetView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedThreshold + parsedCreatedAt.toDouble() + parsedGraceDays.toDouble()
}
