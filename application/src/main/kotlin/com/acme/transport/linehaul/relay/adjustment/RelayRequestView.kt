package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay request. */
data class RelayRequestView(
    var codeText: String,
    var title: String,
    var externalRef: String,
    var endsAt: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toRelayRequestView(item: RelayRequest): RelayRequestView {
    return RelayRequestView(
        codeText = truncate(item.code, 120),
        title = item.title,
        externalRef = item.externalRef,
        endsAt = item.endsAt.toString(),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayRequestViews(items: MutableList<RelayRequest>): MutableList<RelayRequestView> {
    val views = mutableListOf<RelayRequestView>()
    for (item in items) {
        views.add(toRelayRequestView(item))
    }
    return views
}

fun sumRelayRequestView(view: RelayRequestView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
