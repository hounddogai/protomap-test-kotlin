package com.acme.transport.safety.coaching.review

/** How screens and exports show a seasonal coaching budget. */
data class SeasonalCoachingBudgetView(
    var codeText: String,
    var label: String,
    var priority: String,
    var margin: String,
    var ownerIdText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toSeasonalCoachingBudgetView(item: SeasonalCoachingBudget): SeasonalCoachingBudgetView {
    return SeasonalCoachingBudgetView(
        codeText = item.code,
        label = item.label,
        priority = item.priority.toString(),
        margin = "%.2f".format(item.margin),
        ownerIdText = item.ownerId,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCoachingBudgetViews(items: MutableList<SeasonalCoachingBudget>): MutableList<SeasonalCoachingBudgetView> {
    val views = mutableListOf<SeasonalCoachingBudgetView>()
    for (item in items) {
        views.add(toSeasonalCoachingBudgetView(item))
    }
    return views
}

fun sumSeasonalCoachingBudgetView(view: SeasonalCoachingBudgetView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedPriority.toDouble() + parsedMargin + parsedGraceDays.toDouble()
}
