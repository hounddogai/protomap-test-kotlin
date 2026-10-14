package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub budget. */
data class HubBudgetView(
    var codeText: String,
    var title: String,
    var minimum: String,
    var pinned: String,
    var limit: String,
    var createdAt: String,
    var timezone: String,
    var rate: String,
    var statusLabel: String,
)

fun toHubBudgetView(item: HubBudget): HubBudgetView {
    return HubBudgetView(
        codeText = item.code,
        title = truncate(item.title, 40),
        minimum = "%.2f".format(item.minimum),
        pinned = "${item.pinned}",
        limit = item.limit.toString(),
        createdAt = item.createdAt.toString(),
        timezone = truncate(item.timezone, 40),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubBudgetViews(items: MutableList<HubBudget>): MutableList<HubBudgetView> {
    val views = mutableListOf<HubBudgetView>()
    for (item in items) {
        views.add(toHubBudgetView(item))
    }
    return views
}

fun sumHubBudgetView(view: HubBudgetView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedMinimum + parsedLimit.toDouble() + parsedCreatedAt.toDouble()
}
