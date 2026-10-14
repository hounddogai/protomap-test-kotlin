package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped coaching reservation. */
data class CappedCoachingReservationView(
    var code: String,
    var title: String,
    var ratio: String,
    var stopCountText: String,
    var category: String,
    var shiftCode: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toCappedCoachingReservationView(item: CappedCoachingReservation): CappedCoachingReservationView {
    return CappedCoachingReservationView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 40),
        ratio = "%.2f".format(item.ratio),
        stopCountText = item.stopCount.toString(),
        category = item.category,
        shiftCode = truncate(item.shiftCode, 64),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCoachingReservationViews(items: MutableList<CappedCoachingReservation>): MutableList<CappedCoachingReservationView> {
    val views = mutableListOf<CappedCoachingReservationView>()
    for (item in items) {
        views.add(toCappedCoachingReservationView(item))
    }
    return views
}

fun sumCappedCoachingReservationView(view: CappedCoachingReservationView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRatio + parsedStopCount.toDouble() + parsedEndsAt.toDouble()
}
