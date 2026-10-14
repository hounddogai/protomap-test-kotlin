package com.acme.transport.customs.broker.window

/** How screens and exports show a legacy broker window. */
data class LegacyBrokerWindowView(
    var code: String,
    var name: String,
    var contactEmail: String,
    var rate: String,
    var graceDaysText: String,
    var fuelLiters: String,
    var active: String,
    var routeCode: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toLegacyBrokerWindowView(item: LegacyBrokerWindow): LegacyBrokerWindowView {
    return LegacyBrokerWindowView(
        code = item.code,
        name = item.name,
        contactEmail = item.contactEmail,
        rate = "%.2f".format(item.rate),
        graceDaysText = item.graceDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        routeCode = item.routeCode,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyBrokerWindowViews(items: MutableList<LegacyBrokerWindow>): MutableList<LegacyBrokerWindowView> {
    val views = mutableListOf<LegacyBrokerWindowView>()
    for (item in items) {
        views.add(toLegacyBrokerWindowView(item))
    }
    return views
}

fun sumLegacyBrokerWindowView(view: LegacyBrokerWindowView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedRate + parsedGraceDays.toDouble() + parsedFuelLiters
}
