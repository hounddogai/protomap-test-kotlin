package com.acme.transport.fleetops.utilization.budget

/** How screens and exports show a rolling utilization budget. */
data class RollingUtilizationBudgetView(
    var code: String,
    var titleText: String,
    var label: String,
    var amount: String,
    var fuelLiters: String,
    var lane: String,
    var unitText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toRollingUtilizationBudgetView(item: RollingUtilizationBudget): RollingUtilizationBudgetView {
    return RollingUtilizationBudgetView(
        code = item.code,
        titleText = item.title,
        label = item.label,
        amount = "%.2f".format(item.amount),
        fuelLiters = "%.2f".format(item.fuelLiters),
        lane = item.lane,
        unitText = item.unit,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingUtilizationBudgetViews(items: MutableList<RollingUtilizationBudget>): MutableList<RollingUtilizationBudgetView> {
    val views = mutableListOf<RollingUtilizationBudgetView>()
    for (item in items) {
        views.add(toRollingUtilizationBudgetView(item))
    }
    return views
}

fun sumRollingUtilizationBudgetView(view: RollingUtilizationBudgetView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedAmount + parsedFuelLiters + parsedOdometer.toDouble()
}
