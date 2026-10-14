package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped carbon report allocation. */
data class CappedCarbonReportAllocationView(
    var code: String,
    var labelText: String,
    var odometer: String,
    var startsAt: String,
    var attempts: String,
    var segment: String,
    var notes: String,
    var maximum: String,
    var channel: String,
    var statusLabel: String,
)

fun toCappedCarbonReportAllocationView(item: CappedCarbonReportAllocation): CappedCarbonReportAllocationView {
    return CappedCarbonReportAllocationView(
        code = item.code,
        labelText = item.label,
        odometer = item.odometer.toString(),
        startsAt = item.startsAt.toString(),
        attempts = item.attempts.toString(),
        segment = truncate(item.segment, 40),
        notes = truncate(item.notes, 64),
        maximum = "%.2f".format(item.maximum),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCarbonReportAllocationViews(items: MutableList<CappedCarbonReportAllocation>): MutableList<CappedCarbonReportAllocationView> {
    val views = mutableListOf<CappedCarbonReportAllocationView>()
    for (item in items) {
        views.add(toCappedCarbonReportAllocationView(item))
    }
    return views
}

fun sumCappedCarbonReportAllocationView(view: CappedCarbonReportAllocationView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedOdometer.toDouble() + parsedStartsAt.toDouble() + parsedAttempts.toDouble()
}
