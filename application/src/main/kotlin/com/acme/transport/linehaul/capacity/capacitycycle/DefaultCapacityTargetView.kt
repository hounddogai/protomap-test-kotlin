package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default capacity target. */
data class DefaultCapacityTargetView(
    var code: String,
    var title: String,
    var source: String,
    var stopCount: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toDefaultCapacityTargetView(item: DefaultCapacityTarget): DefaultCapacityTargetView {
    return DefaultCapacityTargetView(
        code = truncate(item.code, 120),
        title = item.title,
        source = item.source,
        stopCount = item.stopCount.toString(),
        routeCode = truncate(item.routeCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCapacityTargetViews(items: MutableList<DefaultCapacityTarget>): MutableList<DefaultCapacityTargetView> {
    val views = mutableListOf<DefaultCapacityTargetView>()
    for (item in items) {
        views.add(toDefaultCapacityTargetView(item))
    }
    return views
}

fun sumDefaultCapacityTargetView(view: DefaultCapacityTargetView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
