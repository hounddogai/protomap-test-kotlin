package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dock door claim. */
data class LenientDockDoorClaimView(
    var code: String,
    var name: String,
    var timezoneText: String,
    var segment: String,
    var loadKg: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toLenientDockDoorClaimView(item: LenientDockDoorClaim): LenientDockDoorClaimView {
    return LenientDockDoorClaimView(
        code = item.code,
        name = item.name,
        timezoneText = item.timezone,
        segment = truncate(item.segment, 120),
        loadKg = "%.2f".format(item.loadKg),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDockDoorClaimViews(items: MutableList<LenientDockDoorClaim>): MutableList<LenientDockDoorClaimView> {
    val views = mutableListOf<LenientDockDoorClaimView>()
    for (item in items) {
        views.add(toLenientDockDoorClaimView(item))
    }
    return views
}

fun sumLenientDockDoorClaimView(view: LenientDockDoorClaimView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedLoadKg + parsedGraceDays.toDouble()
}
