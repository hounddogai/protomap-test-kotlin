package com.acme.transport.crewing.roster.rule

/** How screens and exports show a roster rule. */
data class RosterRuleView(
    var code: String,
    var titleText: String,
    var limit: String,
    var routeCodeText: String,
    var externalRef: String,
    var active: String,
    var budgetText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toRosterRuleView(item: RosterRule): RosterRuleView {
    return RosterRuleView(
        code = item.code,
        titleText = item.title,
        limit = item.limit.toString(),
        routeCodeText = item.routeCode,
        externalRef = item.externalRef,
        active = "${item.active}",
        budgetText = "%.2f".format(item.budget),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterRuleViews(items: MutableList<RosterRule>): MutableList<RosterRuleView> {
    val views = mutableListOf<RosterRuleView>()
    for (item in items) {
        views.add(toRosterRuleView(item))
    }
    return views
}

fun sumRosterRuleView(view: RosterRuleView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedLimit.toDouble() + parsedBudget + parsedFloorValue
}
