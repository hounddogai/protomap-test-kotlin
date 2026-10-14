package com.acme.transport.customs.broker.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal broker group. */
data class SeasonalBrokerGroupView(
    var code: String,
    var nameText: String,
    var dockText: String,
    var distanceKmText: String,
    var budget: String,
    var fuelLitersText: String,
    var capacity: String,
    var contactEmailText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toSeasonalBrokerGroupView(item: SeasonalBrokerGroup): SeasonalBrokerGroupView {
    return SeasonalBrokerGroupView(
        code = item.code,
        nameText = truncate(item.name, 40),
        dockText = item.dock,
        distanceKmText = "%.2f".format(item.distanceKm),
        budget = "%.2f".format(item.budget),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        capacity = item.capacity.toString(),
        contactEmailText = item.contactEmail,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalBrokerGroupViews(items: MutableList<SeasonalBrokerGroup>): MutableList<SeasonalBrokerGroupView> {
    val views = mutableListOf<SeasonalBrokerGroupView>()
    for (item in items) {
        views.add(toSeasonalBrokerGroupView(item))
    }
    return views
}

fun sumSeasonalBrokerGroupView(view: SeasonalBrokerGroupView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedDistanceKm + parsedBudget + parsedFuelLiters
}
