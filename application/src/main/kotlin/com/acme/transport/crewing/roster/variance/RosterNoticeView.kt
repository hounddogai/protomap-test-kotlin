package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster notice. */
data class RosterNoticeView(
    var code: String,
    var title: String,
    var floorValue: String,
    var channelText: String,
    var updatedAt: String,
    var margin: String,
    var currency: String,
    var active: String,
    var carrier: String,
    var statusLabel: String,
)

fun toRosterNoticeView(item: RosterNotice): RosterNoticeView {
    return RosterNoticeView(
        code = item.code,
        title = truncate(item.title, 120),
        floorValue = "%.2f".format(item.floorValue),
        channelText = item.channel,
        updatedAt = item.updatedAt.toString(),
        margin = "%.2f".format(item.margin),
        currency = truncate(item.currency, 80),
        active = "${item.active}",
        carrier = truncate(item.carrier, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterNoticeViews(items: MutableList<RosterNotice>): MutableList<RosterNoticeView> {
    val views = mutableListOf<RosterNoticeView>()
    for (item in items) {
        views.add(toRosterNoticeView(item))
    }
    return views
}

fun sumRosterNoticeView(view: RosterNoticeView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedFloorValue + parsedUpdatedAt.toDouble() + parsedMargin
}
