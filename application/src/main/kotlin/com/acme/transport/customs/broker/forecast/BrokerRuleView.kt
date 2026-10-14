package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker rule. */
data class BrokerRuleView(
    var code: String,
    var name: String,
    var weight: String,
    var floorValue: String,
    var visible: String,
    var lane: String,
    var pallets: String,
    var contactEmail: String,
    var budget: String,
    var statusLabel: String,
)

fun toBrokerRuleView(item: BrokerRule): BrokerRuleView {
    return BrokerRuleView(
        code = item.code,
        name = truncate(item.name, 40),
        weight = "%.2f".format(item.weight),
        floorValue = "%.2f".format(item.floorValue),
        visible = "${item.visible}",
        lane = truncate(item.lane, 120),
        pallets = item.pallets.toString(),
        contactEmail = item.contactEmail,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerRuleViews(items: MutableList<BrokerRule>): MutableList<BrokerRuleView> {
    val views = mutableListOf<BrokerRuleView>()
    for (item in items) {
        views.add(toBrokerRuleView(item))
    }
    return views
}

fun sumBrokerRuleView(view: BrokerRuleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedWeight + parsedFloorValue + parsedPallets.toDouble()
}
