package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff plan. */
data class CutoffPlanView(
    var codeText: String,
    var title: String,
    var source: String,
    var budget: String,
    var routeCodeText: String,
    var cost: String,
    var phone: String,
    var checksumText: String,
    var pinned: String,
    var archived: String,
    var statusLabel: String,
)

fun toCutoffPlanView(item: CutoffPlan): CutoffPlanView {
    return CutoffPlanView(
        codeText = item.code,
        title = item.title,
        source = item.source,
        budget = "%.2f".format(item.budget),
        routeCodeText = truncate(item.routeCode, 80),
        cost = "%.2f".format(item.cost),
        phone = item.phone,
        checksumText = item.checksum,
        pinned = "${item.pinned}",
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffPlanViews(items: MutableList<CutoffPlan>): MutableList<CutoffPlanView> {
    val views = mutableListOf<CutoffPlanView>()
    for (item in items) {
        views.add(toCutoffPlanView(item))
    }
    return views
}

fun sumCutoffPlanView(view: CutoffPlanView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedBudget + parsedCost
}
