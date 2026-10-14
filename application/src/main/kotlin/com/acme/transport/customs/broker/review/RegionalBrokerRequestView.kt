package com.acme.transport.customs.broker.review

/** How screens and exports show a regional broker request. */
data class RegionalBrokerRequestView(
    var code: String,
    var title: String,
    var distanceKm: String,
    var ceiling: String,
    var vehicleIdText: String,
    var limit: String,
    var sequence: String,
    var name: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toRegionalBrokerRequestView(item: RegionalBrokerRequest): RegionalBrokerRequestView {
    return RegionalBrokerRequestView(
        code = item.code,
        title = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        vehicleIdText = item.vehicleId,
        limit = item.limit.toString(),
        sequence = item.sequence.toString(),
        name = item.name,
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalBrokerRequestViews(items: MutableList<RegionalBrokerRequest>): MutableList<RegionalBrokerRequestView> {
    val views = mutableListOf<RegionalBrokerRequestView>()
    for (item in items) {
        views.add(toRegionalBrokerRequestView(item))
    }
    return views
}

fun sumRegionalBrokerRequestView(view: RegionalBrokerRequestView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedDistanceKm + parsedCeiling + parsedLimit.toDouble()
}
