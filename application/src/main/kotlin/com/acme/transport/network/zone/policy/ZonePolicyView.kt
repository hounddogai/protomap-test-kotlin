package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone policy. */
data class ZonePolicyView(
    var code: String,
    var name: String,
    var budget: String,
    var graceDays: String,
    var depot: String,
    var currency: String,
    var regionText: String,
    var statusLabel: String,
)

fun toZonePolicyView(item: ZonePolicy): ZonePolicyView {
    return ZonePolicyView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 40),
        budget = "%.2f".format(item.budget),
        graceDays = item.graceDays.toString(),
        depot = item.depot,
        currency = truncate(item.currency, 80),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZonePolicyViews(items: MutableList<ZonePolicy>): MutableList<ZonePolicyView> {
    val views = mutableListOf<ZonePolicyView>()
    for (item in items) {
        views.add(toZonePolicyView(item))
    }
    return views
}

fun sumZonePolicyView(view: ZonePolicyView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedBudget + parsedGraceDays.toDouble()
}
