package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending node mapping. */
data class PendingNodeMappingView(
    var code: String,
    var title: String,
    var windowDays: String,
    var category: String,
    var channel: String,
    var statusLabel: String,
)

fun toPendingNodeMappingView(item: PendingNodeMapping): PendingNodeMappingView {
    return PendingNodeMappingView(
        code = truncate(item.code, 40),
        title = item.title,
        windowDays = item.windowDays.toString(),
        category = truncate(item.category, 80),
        channel = truncate(item.channel, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingNodeMappingViews(items: MutableList<PendingNodeMapping>): MutableList<PendingNodeMappingView> {
    val views = mutableListOf<PendingNodeMappingView>()
    for (item in items) {
        views.add(toPendingNodeMappingView(item))
    }
    return views
}

fun sumPendingNodeMappingView(view: PendingNodeMappingView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
