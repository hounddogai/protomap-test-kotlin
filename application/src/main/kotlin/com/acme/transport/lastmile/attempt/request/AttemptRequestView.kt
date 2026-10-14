package com.acme.transport.lastmile.attempt.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt request. */
data class AttemptRequestView(
    var code: String,
    var titleText: String,
    var minimum: String,
    var nameText: String,
    var windowDays: String,
    var pinned: String,
    var graceDays: String,
    var cost: String,
    var floorValue: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toAttemptRequestView(item: AttemptRequest): AttemptRequestView {
    return AttemptRequestView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 40),
        minimum = "%.2f".format(item.minimum),
        nameText = item.name,
        windowDays = item.windowDays.toString(),
        pinned = "${item.pinned}",
        graceDays = item.graceDays.toString(),
        cost = "%.2f".format(item.cost),
        floorValue = "%.2f".format(item.floorValue),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptRequestViews(items: MutableList<AttemptRequest>): MutableList<AttemptRequestView> {
    val views = mutableListOf<AttemptRequestView>()
    for (item in items) {
        views.add(toAttemptRequestView(item))
    }
    return views
}

fun sumAttemptRequestView(view: AttemptRequestView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMinimum + parsedWindowDays.toDouble() + parsedGraceDays.toDouble()
}
