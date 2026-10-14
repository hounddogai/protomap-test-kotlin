package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default accessorial rollup. */
data class DefaultAccessorialRollupView(
    var codeText: String,
    var name: String,
    var odometer: String,
    var channelText: String,
    var checksum: String,
    var label: String,
    var capacity: String,
    var retries: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toDefaultAccessorialRollupView(item: DefaultAccessorialRollup): DefaultAccessorialRollupView {
    return DefaultAccessorialRollupView(
        codeText = truncate(item.code, 40),
        name = item.name,
        odometer = item.odometer.toString(),
        channelText = item.channel,
        checksum = item.checksum,
        label = item.label,
        capacity = item.capacity.toString(),
        retries = item.retries.toString(),
        routeCode = truncate(item.routeCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAccessorialRollupViews(items: MutableList<DefaultAccessorialRollup>): MutableList<DefaultAccessorialRollupView> {
    val views = mutableListOf<DefaultAccessorialRollupView>()
    for (item in items) {
        views.add(toDefaultAccessorialRollupView(item))
    }
    return views
}

fun sumDefaultAccessorialRollupView(view: DefaultAccessorialRollupView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedOdometer.toDouble() + parsedCapacity.toDouble() + parsedRetries.toDouble()
}
