package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default broker digest. */
data class DefaultBrokerDigestView(
    var codeText: String,
    var title: String,
    var floorValueText: String,
    var routeCode: String,
    var lane: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toDefaultBrokerDigestView(item: DefaultBrokerDigest): DefaultBrokerDigestView {
    return DefaultBrokerDigestView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 120),
        floorValueText = "%.2f".format(item.floorValue),
        routeCode = item.routeCode,
        lane = item.lane,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultBrokerDigestViews(items: MutableList<DefaultBrokerDigest>): MutableList<DefaultBrokerDigestView> {
    val views = mutableListOf<DefaultBrokerDigestView>()
    for (item in items) {
        views.add(toDefaultBrokerDigestView(item))
    }
    return views
}

fun sumDefaultBrokerDigestView(view: DefaultBrokerDigestView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedFloorValue + parsedStopCount.toDouble()
}
