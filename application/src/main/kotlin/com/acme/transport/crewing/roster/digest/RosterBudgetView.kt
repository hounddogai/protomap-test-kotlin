package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster budget. */
data class RosterBudgetView(
    var code: String,
    var titleText: String,
    var score: String,
    var active: String,
    var priority: String,
    var budget: String,
    var startsAtText: String,
    var shiftCodeText: String,
    var region: String,
    var statusLabel: String,
)

fun toRosterBudgetView(item: RosterBudget): RosterBudgetView {
    return RosterBudgetView(
        code = item.code,
        titleText = item.title,
        score = "%.2f".format(item.score),
        active = "${item.active}",
        priority = item.priority.toString(),
        budget = "%.2f".format(item.budget),
        startsAtText = item.startsAt.toString(),
        shiftCodeText = truncate(item.shiftCode, 120),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterBudgetViews(items: MutableList<RosterBudget>): MutableList<RosterBudgetView> {
    val views = mutableListOf<RosterBudgetView>()
    for (item in items) {
        views.add(toRosterBudgetView(item))
    }
    return views
}

fun sumRosterBudgetView(view: RosterBudgetView): Double {
    val parsedScore = view.score.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedScore + parsedPriority.toDouble() + parsedBudget
}
