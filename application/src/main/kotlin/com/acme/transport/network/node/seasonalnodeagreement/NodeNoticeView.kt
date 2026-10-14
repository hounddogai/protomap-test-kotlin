package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node notice. */
data class NodeNoticeView(
    var codeText: String,
    var name: String,
    var expiresAt: String,
    var source: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toNodeNoticeView(item: NodeNotice): NodeNoticeView {
    return NodeNoticeView(
        codeText = truncate(item.code, 40),
        name = item.name,
        expiresAt = item.expiresAt.toString(),
        source = truncate(item.source, 120),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeNoticeViews(items: MutableList<NodeNotice>): MutableList<NodeNoticeView> {
    val views = mutableListOf<NodeNoticeView>()
    for (item in items) {
        views.add(toNodeNoticeView(item))
    }
    return views
}

fun sumNodeNoticeView(view: NodeNoticeView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedExpiresAt.toDouble() + parsedDurationMinutes.toDouble()
}
