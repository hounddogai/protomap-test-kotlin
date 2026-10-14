package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling notice. */
data class IdlingNoticeView(
    var codeText: String,
    var title: String,
    var ownerIdText: String,
    var floorValue: String,
    var startsAtText: String,
    var carrier: String,
    var graceDaysText: String,
    var channel: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toIdlingNoticeView(item: IdlingNotice): IdlingNoticeView {
    return IdlingNoticeView(
        codeText = item.code,
        title = item.title,
        ownerIdText = item.ownerId,
        floorValue = "%.2f".format(item.floorValue),
        startsAtText = item.startsAt.toString(),
        carrier = item.carrier,
        graceDaysText = item.graceDays.toString(),
        channel = truncate(item.channel, 40),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingNoticeViews(items: MutableList<IdlingNotice>): MutableList<IdlingNoticeView> {
    val views = mutableListOf<IdlingNoticeView>()
    for (item in items) {
        views.add(toIdlingNoticeView(item))
    }
    return views
}

fun sumIdlingNoticeView(view: IdlingNoticeView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedFloorValue + parsedStartsAt.toDouble() + parsedGraceDays.toDouble()
}
