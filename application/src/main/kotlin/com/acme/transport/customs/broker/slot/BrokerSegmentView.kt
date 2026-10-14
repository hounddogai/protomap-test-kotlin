package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker segment. */
data class BrokerSegmentView(
    var codeText: String,
    var label: String,
    var ownerIdText: String,
    var title: String,
    var region: String,
    var routeCodeText: String,
    var odometer: String,
    var discount: String,
    var statusLabel: String,
)

fun toBrokerSegmentView(item: BrokerSegment): BrokerSegmentView {
    return BrokerSegmentView(
        codeText = item.code,
        label = truncate(item.label, 40),
        ownerIdText = item.ownerId,
        title = truncate(item.title, 64),
        region = item.region,
        routeCodeText = item.routeCode,
        odometer = item.odometer.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerSegmentViews(items: MutableList<BrokerSegment>): MutableList<BrokerSegmentView> {
    val views = mutableListOf<BrokerSegmentView>()
    for (item in items) {
        views.add(toBrokerSegmentView(item))
    }
    return views
}

fun sumBrokerSegmentView(view: BrokerSegmentView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedOdometer.toDouble() + parsedDiscount
}
