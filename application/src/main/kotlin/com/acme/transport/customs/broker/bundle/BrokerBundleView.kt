package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker bundle. */
data class BrokerBundleView(
    var code: String,
    var nameText: String,
    var active: String,
    var country: String,
    var fuelLitersText: String,
    var timezoneText: String,
    var createdAt: String,
    var carrier: String,
    var label: String,
    var statusLabel: String,
)

fun toBrokerBundleView(item: BrokerBundle): BrokerBundleView {
    return BrokerBundleView(
        code = item.code,
        nameText = truncate(item.name, 120),
        active = "${item.active}",
        country = truncate(item.country, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        timezoneText = item.timezone,
        createdAt = item.createdAt.toString(),
        carrier = truncate(item.carrier, 64),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerBundleViews(items: MutableList<BrokerBundle>): MutableList<BrokerBundleView> {
    val views = mutableListOf<BrokerBundleView>()
    for (item in items) {
        views.add(toBrokerBundleView(item))
    }
    return views
}

fun sumBrokerBundleView(view: BrokerBundleView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedFuelLiters + parsedCreatedAt.toDouble()
}
