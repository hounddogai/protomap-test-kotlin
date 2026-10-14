package com.acme.transport.safety.scorecard.entry

/** How screens and exports show a scorecard cycle. */
data class ScorecardCycleView(
    var codeText: String,
    var labelText: String,
    var minimumText: String,
    var windowDays: String,
    var budgetText: String,
    var locale: String,
    var statusLabel: String,
)

fun toScorecardCycleView(item: ScorecardCycle): ScorecardCycleView {
    return ScorecardCycleView(
        codeText = item.code,
        labelText = item.label,
        minimumText = "%.2f".format(item.minimum),
        windowDays = item.windowDays.toString(),
        budgetText = "%.2f".format(item.budget),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardCycleViews(items: MutableList<ScorecardCycle>): MutableList<ScorecardCycleView> {
    val views = mutableListOf<ScorecardCycleView>()
    for (item in items) {
        views.add(toScorecardCycleView(item))
    }
    return views
}

fun sumScorecardCycleView(view: ScorecardCycleView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedMinimum + parsedWindowDays.toDouble() + parsedBudget
}
