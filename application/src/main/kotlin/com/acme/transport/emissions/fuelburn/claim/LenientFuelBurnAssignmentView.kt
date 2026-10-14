package com.acme.transport.emissions.fuelburn.claim

/** How screens and exports show a lenient fuel burn assignment. */
data class LenientFuelBurnAssignmentView(
    var code: String,
    var title: String,
    var graceDaysText: String,
    var limitText: String,
    var trailerId: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toLenientFuelBurnAssignmentView(item: LenientFuelBurnAssignment): LenientFuelBurnAssignmentView {
    return LenientFuelBurnAssignmentView(
        code = item.code,
        title = item.title,
        graceDaysText = item.graceDays.toString(),
        limitText = item.limit.toString(),
        trailerId = item.trailerId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelBurnAssignmentViews(items: MutableList<LenientFuelBurnAssignment>): MutableList<LenientFuelBurnAssignmentView> {
    val views = mutableListOf<LenientFuelBurnAssignmentView>()
    for (item in items) {
        views.add(toLenientFuelBurnAssignmentView(item))
    }
    return views
}

fun sumLenientFuelBurnAssignmentView(view: LenientFuelBurnAssignmentView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedGraceDays.toDouble() + parsedLimit.toDouble() + parsedFuelLiters
}
