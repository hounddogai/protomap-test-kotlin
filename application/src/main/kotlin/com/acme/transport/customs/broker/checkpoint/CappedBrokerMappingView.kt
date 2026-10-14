package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped broker mapping. */
data class CappedBrokerMappingView(
    var code: String,
    var label: String,
    var driverIdText: String,
    var ownerId: String,
    var distanceKmText: String,
    var carrier: String,
    var contactEmail: String,
    var approved: String,
    var weightText: String,
    var statusLabel: String,
)

fun toCappedBrokerMappingView(item: CappedBrokerMapping): CappedBrokerMappingView {
    return CappedBrokerMappingView(
        code = item.code,
        label = truncate(item.label, 40),
        driverIdText = item.driverId,
        ownerId = item.ownerId,
        distanceKmText = "%.2f".format(item.distanceKm),
        carrier = item.carrier,
        contactEmail = item.contactEmail,
        approved = "${item.approved}",
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedBrokerMappingViews(items: MutableList<CappedBrokerMapping>): MutableList<CappedBrokerMappingView> {
    val views = mutableListOf<CappedBrokerMappingView>()
    for (item in items) {
        views.add(toCappedBrokerMappingView(item))
    }
    return views
}

fun sumCappedBrokerMappingView(view: CappedBrokerMappingView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedDistanceKm + parsedWeight
}
