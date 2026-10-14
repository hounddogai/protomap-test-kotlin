package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy locker adjustment. */
data class LegacyLockerAdjustmentView(
    var codeText: String,
    var title: String,
    var limitText: String,
    var summary: String,
    var notes: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toLegacyLockerAdjustmentView(item: LegacyLockerAdjustment): LegacyLockerAdjustmentView {
    return LegacyLockerAdjustmentView(
        codeText = item.code,
        title = item.title,
        limitText = item.limit.toString(),
        summary = item.summary,
        notes = truncate(item.notes, 64),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLockerAdjustmentViews(items: MutableList<LegacyLockerAdjustment>): MutableList<LegacyLockerAdjustmentView> {
    val views = mutableListOf<LegacyLockerAdjustmentView>()
    for (item in items) {
        views.add(toLegacyLockerAdjustmentView(item))
    }
    return views
}

fun sumLegacyLockerAdjustmentView(view: LegacyLockerAdjustmentView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedLimit.toDouble() + parsedDurationMinutes.toDouble()
}
