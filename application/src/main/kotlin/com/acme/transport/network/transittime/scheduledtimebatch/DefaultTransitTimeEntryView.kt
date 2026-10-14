package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default transit time entry. */
data class DefaultTransitTimeEntryView(
    var code: String,
    var titleText: String,
    var visible: String,
    var startsAtText: String,
    var minimum: String,
    var lane: String,
    var statusLabel: String,
)

fun toDefaultTransitTimeEntryView(item: DefaultTransitTimeEntry): DefaultTransitTimeEntryView {
    return DefaultTransitTimeEntryView(
        code = item.code,
        titleText = item.title,
        visible = "${item.visible}",
        startsAtText = item.startsAt.toString(),
        minimum = "%.2f".format(item.minimum),
        lane = truncate(item.lane, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTransitTimeEntryViews(items: MutableList<DefaultTransitTimeEntry>): MutableList<DefaultTransitTimeEntryView> {
    val views = mutableListOf<DefaultTransitTimeEntryView>()
    for (item in items) {
        views.add(toDefaultTransitTimeEntryView(item))
    }
    return views
}

fun sumDefaultTransitTimeEntryView(view: DefaultTransitTimeEntryView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStartsAt.toDouble() + parsedMinimum
}
