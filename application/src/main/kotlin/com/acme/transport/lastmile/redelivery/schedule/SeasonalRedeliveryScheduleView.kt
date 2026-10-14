package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery schedule. */
data class SeasonalRedeliveryScheduleView(
    var code: String,
    var name: String,
    var ratio: String,
    var createdAt: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryScheduleView(item: SeasonalRedeliverySchedule): SeasonalRedeliveryScheduleView {
    return SeasonalRedeliveryScheduleView(
        code = truncate(item.code, 40),
        name = item.name,
        ratio = "%.2f".format(item.ratio),
        createdAt = item.createdAt.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryScheduleViews(items: MutableList<SeasonalRedeliverySchedule>): MutableList<SeasonalRedeliveryScheduleView> {
    val views = mutableListOf<SeasonalRedeliveryScheduleView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryScheduleView(item))
    }
    return views
}

fun sumSeasonalRedeliveryScheduleView(view: SeasonalRedeliveryScheduleView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedRatio + parsedCreatedAt.toDouble()
}
