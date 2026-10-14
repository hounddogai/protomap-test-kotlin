package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender schedule. */
data class TenderScheduleView(
    var code: String,
    var labelText: String,
    var minimum: String,
    var stopCountText: String,
    var pinnedText: String,
    var expiresAtText: String,
    var shiftCode: String,
    var cost: String,
    var ceiling: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toTenderScheduleView(item: TenderSchedule): TenderScheduleView {
    return TenderScheduleView(
        code = truncate(item.code, 64),
        labelText = item.label,
        minimum = "%.2f".format(item.minimum),
        stopCountText = item.stopCount.toString(),
        pinnedText = "${item.pinned}",
        expiresAtText = item.expiresAt.toString(),
        shiftCode = item.shiftCode,
        cost = "%.2f".format(item.cost),
        ceiling = "%.2f".format(item.ceiling),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderScheduleViews(items: MutableList<TenderSchedule>): MutableList<TenderScheduleView> {
    val views = mutableListOf<TenderScheduleView>()
    for (item in items) {
        views.add(toTenderScheduleView(item))
    }
    return views
}

fun sumTenderScheduleView(view: TenderScheduleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedMinimum + parsedStopCount.toDouble() + parsedExpiresAt.toDouble()
}
