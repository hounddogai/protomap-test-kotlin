package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped roster request. */
data class CappedRosterRequestView(
    var code: String,
    var nameText: String,
    var reviewer: String,
    var budget: String,
    var endsAt: String,
    var approved: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toCappedRosterRequestView(item: CappedRosterRequest): CappedRosterRequestView {
    return CappedRosterRequestView(
        code = item.code,
        nameText = item.name,
        reviewer = item.reviewer,
        budget = "%.2f".format(item.budget),
        endsAt = item.endsAt.toString(),
        approved = "${item.approved}",
        routeCode = truncate(item.routeCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRosterRequestViews(items: MutableList<CappedRosterRequest>): MutableList<CappedRosterRequestView> {
    val views = mutableListOf<CappedRosterRequestView>()
    for (item in items) {
        views.add(toCappedRosterRequestView(item))
    }
    return views
}

fun sumCappedRosterRequestView(view: CappedRosterRequestView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedBudget + parsedEndsAt.toDouble()
}
