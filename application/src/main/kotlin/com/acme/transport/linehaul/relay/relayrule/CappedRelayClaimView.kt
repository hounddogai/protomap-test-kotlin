package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped relay claim. */
data class CappedRelayClaimView(
    var codeText: String,
    var name: String,
    var laneText: String,
    var windowDaysText: String,
    var endsAt: String,
    var durationMinutesText: String,
    var graceDays: String,
    var depot: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toCappedRelayClaimView(item: CappedRelayClaim): CappedRelayClaimView {
    return CappedRelayClaimView(
        codeText = item.code,
        name = item.name,
        laneText = truncate(item.lane, 120),
        windowDaysText = item.windowDays.toString(),
        endsAt = item.endsAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        graceDays = item.graceDays.toString(),
        depot = item.depot,
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRelayClaimViews(items: MutableList<CappedRelayClaim>): MutableList<CappedRelayClaimView> {
    val views = mutableListOf<CappedRelayClaimView>()
    for (item in items) {
        views.add(toCappedRelayClaimView(item))
    }
    return views
}

fun sumCappedRelayClaimView(view: CappedRelayClaimView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedWindowDays.toDouble() + parsedEndsAt.toDouble() + parsedDurationMinutes.toDouble()
}
