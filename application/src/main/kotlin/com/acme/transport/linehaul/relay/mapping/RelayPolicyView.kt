package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay policy. */
data class RelayPolicyView(
    var codeText: String,
    var label: String,
    var endsAt: String,
    var floorValue: String,
    var quantity: String,
    var driverId: String,
    var localeText: String,
    var routeCode: String,
    var threshold: String,
    var titleText: String,
    var limit: String,
    var statusLabel: String,
)

fun toRelayPolicyView(item: RelayPolicy): RelayPolicyView {
    return RelayPolicyView(
        codeText = item.code,
        label = item.label,
        endsAt = item.endsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        quantity = item.quantity.toString(),
        driverId = item.driverId,
        localeText = truncate(item.locale, 120),
        routeCode = item.routeCode,
        threshold = "%.2f".format(item.threshold),
        titleText = truncate(item.title, 40),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayPolicyViews(items: MutableList<RelayPolicy>): MutableList<RelayPolicyView> {
    val views = mutableListOf<RelayPolicyView>()
    for (item in items) {
        views.add(toRelayPolicyView(item))
    }
    return views
}

fun sumRelayPolicyView(view: RelayPolicyView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedEndsAt.toDouble() + parsedFloorValue + parsedQuantity.toDouble()
}
