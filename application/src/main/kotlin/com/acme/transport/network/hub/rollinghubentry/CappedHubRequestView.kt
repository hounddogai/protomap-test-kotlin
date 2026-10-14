package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped hub request. */
data class CappedHubRequestView(
    var code: String,
    var label: String,
    var startsAtText: String,
    var contactEmail: String,
    var budget: String,
    var cost: String,
    var statusLabel: String,
)

fun toCappedHubRequestView(item: CappedHubRequest): CappedHubRequestView {
    return CappedHubRequestView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 64),
        startsAtText = item.startsAt.toString(),
        contactEmail = item.contactEmail,
        budget = "%.2f".format(item.budget),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHubRequestViews(items: MutableList<CappedHubRequest>): MutableList<CappedHubRequestView> {
    val views = mutableListOf<CappedHubRequestView>()
    for (item in items) {
        views.add(toCappedHubRequestView(item))
    }
    return views
}

fun sumCappedHubRequestView(view: CappedHubRequestView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedBudget + parsedCost
}
