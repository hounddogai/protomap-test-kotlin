package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled hub rule. */
data class ScheduledHubRuleView(
    var codeText: String,
    var name: String,
    var active: String,
    var loadKg: String,
    var vehicleIdText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toScheduledHubRuleView(item: ScheduledHubRule): ScheduledHubRuleView {
    return ScheduledHubRuleView(
        codeText = truncate(item.code, 64),
        name = truncate(item.name, 80),
        active = "${item.active}",
        loadKg = "%.2f".format(item.loadKg),
        vehicleIdText = item.vehicleId,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHubRuleViews(items: MutableList<ScheduledHubRule>): MutableList<ScheduledHubRuleView> {
    val views = mutableListOf<ScheduledHubRuleView>()
    for (item in items) {
        views.add(toScheduledHubRuleView(item))
    }
    return views
}

fun sumScheduledHubRuleView(view: ScheduledHubRuleView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedLoadKg + parsedAmount
}
