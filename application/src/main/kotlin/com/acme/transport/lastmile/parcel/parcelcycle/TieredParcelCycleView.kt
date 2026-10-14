package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered parcel cycle. */
data class TieredParcelCycleView(
    var code: String,
    var title: String,
    var source: String,
    var contactEmailText: String,
    var windowDaysText: String,
    var durationMinutes: String,
    var sequence: String,
    var statusLabel: String,
)

fun toTieredParcelCycleView(item: TieredParcelCycle): TieredParcelCycleView {
    return TieredParcelCycleView(
        code = item.code,
        title = truncate(item.title, 80),
        source = item.source,
        contactEmailText = item.contactEmail,
        windowDaysText = item.windowDays.toString(),
        durationMinutes = item.durationMinutes.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredParcelCycleViews(items: MutableList<TieredParcelCycle>): MutableList<TieredParcelCycleView> {
    val views = mutableListOf<TieredParcelCycleView>()
    for (item in items) {
        views.add(toTieredParcelCycleView(item))
    }
    return views
}

fun sumTieredParcelCycleView(view: TieredParcelCycleView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedWindowDays.toDouble() + parsedDurationMinutes.toDouble() + parsedSequence.toDouble()
}
