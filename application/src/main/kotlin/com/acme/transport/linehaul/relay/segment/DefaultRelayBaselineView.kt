package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default relay baseline. */
data class DefaultRelayBaselineView(
    var code: String,
    var labelText: String,
    var discountText: String,
    var categoryText: String,
    var dock: String,
    var segment: String,
    var laneText: String,
    var description: String,
    var reviewer: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toDefaultRelayBaselineView(item: DefaultRelayBaseline): DefaultRelayBaselineView {
    return DefaultRelayBaselineView(
        code = item.code,
        labelText = item.label,
        discountText = "%.2f".format(item.discount),
        categoryText = item.category,
        dock = item.dock,
        segment = truncate(item.segment, 64),
        laneText = item.lane,
        description = truncate(item.description, 120),
        reviewer = item.reviewer,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRelayBaselineViews(items: MutableList<DefaultRelayBaseline>): MutableList<DefaultRelayBaselineView> {
    val views = mutableListOf<DefaultRelayBaselineView>()
    for (item in items) {
        views.add(toDefaultRelayBaselineView(item))
    }
    return views
}

fun sumDefaultRelayBaselineView(view: DefaultRelayBaselineView): Double {
    val parsedDiscount = view.discountText.toDouble()
    return parsedDiscount
}
