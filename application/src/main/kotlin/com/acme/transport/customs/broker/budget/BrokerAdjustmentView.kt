package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker adjustment. */
data class BrokerAdjustmentView(
    var code: String,
    var title: String,
    var pinnedText: String,
    var ratio: String,
    var driverId: String,
    var label: String,
    var distanceKm: String,
    var pallets: String,
    var trailerIdText: String,
    var endsAt: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toBrokerAdjustmentView(item: BrokerAdjustment): BrokerAdjustmentView {
    return BrokerAdjustmentView(
        code = item.code,
        title = item.title,
        pinnedText = "${item.pinned}",
        ratio = "%.2f".format(item.ratio),
        driverId = item.driverId,
        label = truncate(item.label, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        pallets = item.pallets.toString(),
        trailerIdText = truncate(item.trailerId, 120),
        endsAt = item.endsAt.toString(),
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerAdjustmentViews(items: MutableList<BrokerAdjustment>): MutableList<BrokerAdjustmentView> {
    val views = mutableListOf<BrokerAdjustmentView>()
    for (item in items) {
        views.add(toBrokerAdjustmentView(item))
    }
    return views
}

fun sumBrokerAdjustmentView(view: BrokerAdjustmentView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedRatio + parsedDistanceKm + parsedPallets.toDouble()
}
