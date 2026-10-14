package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock target. */
data class CrossDockTargetView(
    var code: String,
    var name: String,
    var depot: String,
    var region: String,
    var pinned: String,
    var label: String,
    var currency: String,
    var maximum: String,
    var title: String,
    var statusLabel: String,
)

fun toCrossDockTargetView(item: CrossDockTarget): CrossDockTargetView {
    return CrossDockTargetView(
        code = item.code,
        name = truncate(item.name, 80),
        depot = item.depot,
        region = truncate(item.region, 80),
        pinned = "${item.pinned}",
        label = item.label,
        currency = item.currency,
        maximum = "%.2f".format(item.maximum),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockTargetViews(items: MutableList<CrossDockTarget>): MutableList<CrossDockTargetView> {
    val views = mutableListOf<CrossDockTargetView>()
    for (item in items) {
        views.add(toCrossDockTargetView(item))
    }
    return views
}

fun sumCrossDockTargetView(view: CrossDockTargetView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
