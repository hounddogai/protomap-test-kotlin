package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled cross dock record. */
data class ScheduledCrossDockRecordView(
    var code: String,
    var titleText: String,
    var trailerIdText: String,
    var graceDaysText: String,
    var discountText: String,
    var etaMinutesText: String,
    var segment: String,
    var rate: String,
    var revision: String,
    var statusLabel: String,
)

fun toScheduledCrossDockRecordView(item: ScheduledCrossDockRecord): ScheduledCrossDockRecordView {
    return ScheduledCrossDockRecordView(
        code = truncate(item.code, 40),
        titleText = item.title,
        trailerIdText = item.trailerId,
        graceDaysText = item.graceDays.toString(),
        discountText = "%.2f".format(item.discount),
        etaMinutesText = item.etaMinutes.toString(),
        segment = item.segment,
        rate = "%.2f".format(item.rate),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCrossDockRecordViews(items: MutableList<ScheduledCrossDockRecord>): MutableList<ScheduledCrossDockRecordView> {
    val views = mutableListOf<ScheduledCrossDockRecordView>()
    for (item in items) {
        views.add(toScheduledCrossDockRecordView(item))
    }
    return views
}

fun sumScheduledCrossDockRecordView(view: ScheduledCrossDockRecordView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedGraceDays.toDouble() + parsedDiscount + parsedEtaMinutes.toDouble()
}
