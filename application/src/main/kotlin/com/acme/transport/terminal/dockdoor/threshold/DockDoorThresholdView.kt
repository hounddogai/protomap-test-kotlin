package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door threshold. */
data class DockDoorThresholdView(
    var codeText: String,
    var name: String,
    var active: String,
    var routeCode: String,
    var phone: String,
    var ratioText: String,
    var rateText: String,
    var endsAt: String,
    var channel: String,
    var statusLabel: String,
)

fun toDockDoorThresholdView(item: DockDoorThreshold): DockDoorThresholdView {
    return DockDoorThresholdView(
        codeText = item.code,
        name = item.name,
        active = "${item.active}",
        routeCode = item.routeCode,
        phone = item.phone,
        ratioText = "%.2f".format(item.ratio),
        rateText = "%.2f".format(item.rate),
        endsAt = item.endsAt.toString(),
        channel = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorThresholdViews(items: MutableList<DockDoorThreshold>): MutableList<DockDoorThresholdView> {
    val views = mutableListOf<DockDoorThresholdView>()
    for (item in items) {
        views.add(toDockDoorThresholdView(item))
    }
    return views
}

fun sumDockDoorThresholdView(view: DockDoorThresholdView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRatio + parsedRate + parsedEndsAt.toDouble()
}
