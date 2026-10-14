package com.acme.transport.customs.hold.summary

/** How screens and exports show a hold summary. */
data class HoldSummaryView(
    var code: String,
    var name: String,
    var odometer: String,
    var budget: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toHoldSummaryView(item: HoldSummary): HoldSummaryView {
    return HoldSummaryView(
        code = item.code,
        name = item.name,
        odometer = item.odometer.toString(),
        budget = "%.2f".format(item.budget),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldSummaryViews(items: MutableList<HoldSummary>): MutableList<HoldSummaryView> {
    val views = mutableListOf<HoldSummaryView>()
    for (item in items) {
        views.add(toHoldSummaryView(item))
    }
    return views
}

fun sumHoldSummaryView(view: HoldSummaryView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedOdometer.toDouble() + parsedBudget + parsedGraceDays.toDouble()
}
