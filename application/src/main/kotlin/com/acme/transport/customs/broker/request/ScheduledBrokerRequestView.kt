package com.acme.transport.customs.broker.request

/** How screens and exports show a scheduled broker request. */
data class ScheduledBrokerRequestView(
    var code: String,
    var name: String,
    var floorValue: String,
    var titleText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toScheduledBrokerRequestView(item: ScheduledBrokerRequest): ScheduledBrokerRequestView {
    return ScheduledBrokerRequestView(
        code = item.code,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        titleText = item.title,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledBrokerRequestViews(items: MutableList<ScheduledBrokerRequest>): MutableList<ScheduledBrokerRequestView> {
    val views = mutableListOf<ScheduledBrokerRequestView>()
    for (item in items) {
        views.add(toScheduledBrokerRequestView(item))
    }
    return views
}

fun sumScheduledBrokerRequestView(view: ScheduledBrokerRequestView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedFloorValue + parsedDistanceKm
}
