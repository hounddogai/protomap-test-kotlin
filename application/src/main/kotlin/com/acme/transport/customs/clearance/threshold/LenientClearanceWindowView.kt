package com.acme.transport.customs.clearance.threshold

/** How screens and exports show a lenient clearance window. */
data class LenientClearanceWindowView(
    var code: String,
    var labelText: String,
    var budget: String,
    var cost: String,
    var description: String,
    var vehicleId: String,
    var graceDays: String,
    var sequence: String,
    var statusLabel: String,
)

fun toLenientClearanceWindowView(item: LenientClearanceWindow): LenientClearanceWindowView {
    return LenientClearanceWindowView(
        code = item.code,
        labelText = item.label,
        budget = "%.2f".format(item.budget),
        cost = "%.2f".format(item.cost),
        description = item.description,
        vehicleId = item.vehicleId,
        graceDays = item.graceDays.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientClearanceWindowViews(items: MutableList<LenientClearanceWindow>): MutableList<LenientClearanceWindowView> {
    val views = mutableListOf<LenientClearanceWindowView>()
    for (item in items) {
        views.add(toLenientClearanceWindowView(item))
    }
    return views
}

fun sumLenientClearanceWindowView(view: LenientClearanceWindowView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedBudget + parsedCost + parsedGraceDays.toDouble()
}
