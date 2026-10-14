package com.acme.transport.customs.broker.estimate

/** How screens and exports show a broker group. */
data class BrokerGroupView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var startsAt: String,
    var phoneText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toBrokerGroupView(item: BrokerGroup): BrokerGroupView {
    return BrokerGroupView(
        code = item.code,
        name = item.name,
        thresholdText = "%.2f".format(item.threshold),
        startsAt = item.startsAt.toString(),
        phoneText = item.phone,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerGroupViews(items: MutableList<BrokerGroup>): MutableList<BrokerGroupView> {
    val views = mutableListOf<BrokerGroupView>()
    for (item in items) {
        views.add(toBrokerGroupView(item))
    }
    return views
}

fun sumBrokerGroupView(view: BrokerGroupView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedThreshold + parsedStartsAt.toDouble() + parsedDistanceKm
}
