package com.acme.transport.customs.broker.schedule

/** How screens and exports show a broker entry. */
data class BrokerEntryView(
    var code: String,
    var nameText: String,
    var currency: String,
    var active: String,
    var startsAtText: String,
    var source: String,
    var pallets: String,
    var ceiling: String,
    var odometer: String,
    var statusLabel: String,
)

fun toBrokerEntryView(item: BrokerEntry): BrokerEntryView {
    return BrokerEntryView(
        code = item.code,
        nameText = item.name,
        currency = item.currency,
        active = "${item.active}",
        startsAtText = item.startsAt.toString(),
        source = item.source,
        pallets = item.pallets.toString(),
        ceiling = "%.2f".format(item.ceiling),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerEntryViews(items: MutableList<BrokerEntry>): MutableList<BrokerEntryView> {
    val views = mutableListOf<BrokerEntryView>()
    for (item in items) {
        views.add(toBrokerEntryView(item))
    }
    return views
}

fun sumBrokerEntryView(view: BrokerEntryView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStartsAt.toDouble() + parsedPallets.toDouble() + parsedCeiling
}
