package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback scorecard slot. */
data class FallbackScorecardSlotView(
    var code: String,
    var title: String,
    var startsAt: String,
    var currency: String,
    var batchSize: String,
    var contactEmail: String,
    var durationMinutes: String,
    var country: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toFallbackScorecardSlotView(item: FallbackScorecardSlot): FallbackScorecardSlotView {
    return FallbackScorecardSlotView(
        code = truncate(item.code, 120),
        title = item.title,
        startsAt = item.startsAt.toString(),
        currency = item.currency,
        batchSize = item.batchSize.toString(),
        contactEmail = item.contactEmail,
        durationMinutes = item.durationMinutes.toString(),
        country = item.country,
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackScorecardSlotViews(items: MutableList<FallbackScorecardSlot>): MutableList<FallbackScorecardSlotView> {
    val views = mutableListOf<FallbackScorecardSlotView>()
    for (item in items) {
        views.add(toFallbackScorecardSlotView(item))
    }
    return views
}

fun sumFallbackScorecardSlotView(view: FallbackScorecardSlotView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedStartsAt.toDouble() + parsedBatchSize.toDouble() + parsedDurationMinutes.toDouble()
}
