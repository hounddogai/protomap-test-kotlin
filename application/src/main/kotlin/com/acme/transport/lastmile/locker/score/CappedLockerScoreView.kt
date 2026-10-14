package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped locker score. */
data class CappedLockerScoreView(
    var codeText: String,
    var label: String,
    var windowDays: String,
    var graceDaysText: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toCappedLockerScoreView(item: CappedLockerScore): CappedLockerScoreView {
    return CappedLockerScoreView(
        codeText = item.code,
        label = truncate(item.label, 40),
        windowDays = item.windowDays.toString(),
        graceDaysText = item.graceDays.toString(),
        categoryText = truncate(item.category, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLockerScoreViews(items: MutableList<CappedLockerScore>): MutableList<CappedLockerScoreView> {
    val views = mutableListOf<CappedLockerScoreView>()
    for (item in items) {
        views.add(toCappedLockerScoreView(item))
    }
    return views
}

fun sumCappedLockerScoreView(view: CappedLockerScoreView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedWindowDays.toDouble() + parsedGraceDays.toDouble()
}
