package com.acme.transport.crewing.roster.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending roster tier. */
data class PendingRosterTierView(
    var codeText: String,
    var name: String,
    var durationMinutes: String,
    var depot: String,
    var phone: String,
    var weightText: String,
    var stopCountText: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toPendingRosterTierView(item: PendingRosterTier): PendingRosterTierView {
    return PendingRosterTierView(
        codeText = item.code,
        name = truncate(item.name, 40),
        durationMinutes = item.durationMinutes.toString(),
        depot = item.depot,
        phone = item.phone,
        weightText = "%.2f".format(item.weight),
        stopCountText = item.stopCount.toString(),
        shiftCodeText = truncate(item.shiftCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRosterTierViews(items: MutableList<PendingRosterTier>): MutableList<PendingRosterTierView> {
    val views = mutableListOf<PendingRosterTierView>()
    for (item in items) {
        views.add(toPendingRosterTierView(item))
    }
    return views
}

fun sumPendingRosterTierView(view: PendingRosterTierView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedDurationMinutes.toDouble() + parsedWeight + parsedStopCount.toDouble()
}
