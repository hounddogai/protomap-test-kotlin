package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global broker request. */
data class GlobalBrokerRequestView(
    var code: String,
    var label: String,
    var fuelLiters: String,
    var costText: String,
    var contactEmail: String,
    var loadKg: String,
    var timezone: String,
    var statusLabel: String,
)

fun toGlobalBrokerRequestView(item: GlobalBrokerRequest): GlobalBrokerRequestView {
    return GlobalBrokerRequestView(
        code = truncate(item.code, 120),
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        costText = "%.2f".format(item.cost),
        contactEmail = truncate(item.contactEmail, 40),
        loadKg = "%.2f".format(item.loadKg),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBrokerRequestViews(items: MutableList<GlobalBrokerRequest>): MutableList<GlobalBrokerRequestView> {
    val views = mutableListOf<GlobalBrokerRequestView>()
    for (item in items) {
        views.add(toGlobalBrokerRequestView(item))
    }
    return views
}

fun sumGlobalBrokerRequestView(view: GlobalBrokerRequestView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCost = view.costText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedFuelLiters + parsedCost + parsedLoadKg
}
