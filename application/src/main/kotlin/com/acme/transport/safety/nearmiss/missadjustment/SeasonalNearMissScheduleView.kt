package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal near miss schedule. */
data class SeasonalNearMissScheduleView(
    var codeText: String,
    var label: String,
    var minimum: String,
    var checksum: String,
    var depot: String,
    var channel: String,
    var category: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toSeasonalNearMissScheduleView(item: SeasonalNearMissSchedule): SeasonalNearMissScheduleView {
    return SeasonalNearMissScheduleView(
        codeText = truncate(item.code, 120),
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        checksum = truncate(item.checksum, 64),
        depot = item.depot,
        channel = item.channel,
        category = item.category,
        shiftCode = truncate(item.shiftCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalNearMissScheduleViews(items: MutableList<SeasonalNearMissSchedule>): MutableList<SeasonalNearMissScheduleView> {
    val views = mutableListOf<SeasonalNearMissScheduleView>()
    for (item in items) {
        views.add(toSeasonalNearMissScheduleView(item))
    }
    return views
}

fun sumSeasonalNearMissScheduleView(view: SeasonalNearMissScheduleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
