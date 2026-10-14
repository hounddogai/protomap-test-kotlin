package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy defect cycle. */
data class LegacyDefectCycleView(
    var code: String,
    var title: String,
    var durationMinutesText: String,
    var driverId: String,
    var approvedText: String,
    var phone: String,
    var minimum: String,
    var regionText: String,
    var statusLabel: String,
)

fun toLegacyDefectCycleView(item: LegacyDefectCycle): LegacyDefectCycleView {
    return LegacyDefectCycleView(
        code = item.code,
        title = item.title,
        durationMinutesText = item.durationMinutes.toString(),
        driverId = item.driverId,
        approvedText = "${item.approved}",
        phone = truncate(item.phone, 120),
        minimum = "%.2f".format(item.minimum),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDefectCycleViews(items: MutableList<LegacyDefectCycle>): MutableList<LegacyDefectCycleView> {
    val views = mutableListOf<LegacyDefectCycleView>()
    for (item in items) {
        views.add(toLegacyDefectCycleView(item))
    }
    return views
}

fun sumLegacyDefectCycleView(view: LegacyDefectCycleView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedDurationMinutes.toDouble() + parsedMinimum
}
