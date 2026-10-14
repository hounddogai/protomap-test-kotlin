package com.acme.transport.network.hub.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered hub exception. */
data class TieredHubExceptionView(
    var codeText: String,
    var name: String,
    var windowDays: String,
    var visible: String,
    var lane: String,
    var sequence: String,
    var endsAt: String,
    var maximum: String,
    var margin: String,
    var statusLabel: String,
)

fun toTieredHubExceptionView(item: TieredHubException): TieredHubExceptionView {
    return TieredHubExceptionView(
        codeText = truncate(item.code, 40),
        name = item.name,
        windowDays = item.windowDays.toString(),
        visible = "${item.visible}",
        lane = truncate(item.lane, 40),
        sequence = item.sequence.toString(),
        endsAt = item.endsAt.toString(),
        maximum = "%.2f".format(item.maximum),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredHubExceptionViews(items: MutableList<TieredHubException>): MutableList<TieredHubExceptionView> {
    val views = mutableListOf<TieredHubExceptionView>()
    for (item in items) {
        views.add(toTieredHubExceptionView(item))
    }
    return views
}

fun sumTieredHubExceptionView(view: TieredHubExceptionView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedWindowDays.toDouble() + parsedSequence.toDouble() + parsedEndsAt.toDouble()
}
