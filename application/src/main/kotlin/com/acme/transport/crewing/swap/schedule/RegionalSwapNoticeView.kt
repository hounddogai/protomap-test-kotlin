package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional swap notice. */
data class RegionalSwapNoticeView(
    var code: String,
    var label: String,
    var fuelLitersText: String,
    var startsAt: String,
    var ownerId: String,
    var margin: String,
    var amount: String,
    var notes: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toRegionalSwapNoticeView(item: RegionalSwapNotice): RegionalSwapNoticeView {
    return RegionalSwapNoticeView(
        code = item.code,
        label = item.label,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        startsAt = item.startsAt.toString(),
        ownerId = item.ownerId,
        margin = "%.2f".format(item.margin),
        amount = "%.2f".format(item.amount),
        notes = truncate(item.notes, 64),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalSwapNoticeViews(items: MutableList<RegionalSwapNotice>): MutableList<RegionalSwapNoticeView> {
    val views = mutableListOf<RegionalSwapNoticeView>()
    for (item in items) {
        views.add(toRegionalSwapNoticeView(item))
    }
    return views
}

fun sumRegionalSwapNoticeView(view: RegionalSwapNoticeView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedFuelLiters + parsedStartsAt.toDouble() + parsedMargin
}
