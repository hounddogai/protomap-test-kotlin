package com.acme.transport.customs.clearance.milestone

/** How screens and exports show a seasonal clearance milestone. */
data class SeasonalClearanceMilestoneView(
    var code: String,
    var name: String,
    var archivedText: String,
    var locale: String,
    var capacity: String,
    var sourceText: String,
    var budgetText: String,
    var windowDays: String,
    var phone: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toSeasonalClearanceMilestoneView(item: SeasonalClearanceMilestone): SeasonalClearanceMilestoneView {
    return SeasonalClearanceMilestoneView(
        code = item.code,
        name = item.name,
        archivedText = "${item.archived}",
        locale = item.locale,
        capacity = item.capacity.toString(),
        sourceText = item.source,
        budgetText = "%.2f".format(item.budget),
        windowDays = item.windowDays.toString(),
        phone = item.phone,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalClearanceMilestoneViews(items: MutableList<SeasonalClearanceMilestone>): MutableList<SeasonalClearanceMilestoneView> {
    val views = mutableListOf<SeasonalClearanceMilestoneView>()
    for (item in items) {
        views.add(toSeasonalClearanceMilestoneView(item))
    }
    return views
}

fun sumSeasonalClearanceMilestoneView(view: SeasonalClearanceMilestoneView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedCapacity.toDouble() + parsedBudget + parsedWindowDays.toDouble()
}
