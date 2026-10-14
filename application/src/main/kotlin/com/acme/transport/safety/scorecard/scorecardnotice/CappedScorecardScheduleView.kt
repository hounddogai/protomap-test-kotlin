package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped scorecard schedule. */
data class CappedScorecardScheduleView(
    var codeText: String,
    var nameText: String,
    var routeCode: String,
    var ratio: String,
    var channelText: String,
    var maximumText: String,
    var shiftCode: String,
    var quantity: String,
    var label: String,
    var category: String,
    var statusLabel: String,
)

fun toCappedScorecardScheduleView(item: CappedScorecardSchedule): CappedScorecardScheduleView {
    return CappedScorecardScheduleView(
        codeText = item.code,
        nameText = item.name,
        routeCode = item.routeCode,
        ratio = "%.2f".format(item.ratio),
        channelText = item.channel,
        maximumText = "%.2f".format(item.maximum),
        shiftCode = truncate(item.shiftCode, 64),
        quantity = item.quantity.toString(),
        label = item.label,
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedScorecardScheduleViews(items: MutableList<CappedScorecardSchedule>): MutableList<CappedScorecardScheduleView> {
    val views = mutableListOf<CappedScorecardScheduleView>()
    for (item in items) {
        views.add(toCappedScorecardScheduleView(item))
    }
    return views
}

fun sumCappedScorecardScheduleView(view: CappedScorecardScheduleView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedRatio + parsedMaximum + parsedQuantity.toDouble()
}
