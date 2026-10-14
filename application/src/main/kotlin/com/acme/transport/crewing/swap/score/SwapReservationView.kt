package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap reservation. */
data class SwapReservationView(
    var code: String,
    var labelText: String,
    var graceDaysText: String,
    var revision: String,
    var updatedAtText: String,
    var locale: String,
    var visibleText: String,
    var checksum: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toSwapReservationView(item: SwapReservation): SwapReservationView {
    return SwapReservationView(
        code = item.code,
        labelText = item.label,
        graceDaysText = item.graceDays.toString(),
        revision = item.revision.toString(),
        updatedAtText = item.updatedAt.toString(),
        locale = truncate(item.locale, 40),
        visibleText = "${item.visible}",
        checksum = item.checksum,
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapReservationViews(items: MutableList<SwapReservation>): MutableList<SwapReservationView> {
    val views = mutableListOf<SwapReservationView>()
    for (item in items) {
        views.add(toSwapReservationView(item))
    }
    return views
}

fun sumSwapReservationView(view: SwapReservationView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedGraceDays.toDouble() + parsedRevision.toDouble() + parsedUpdatedAt.toDouble()
}
