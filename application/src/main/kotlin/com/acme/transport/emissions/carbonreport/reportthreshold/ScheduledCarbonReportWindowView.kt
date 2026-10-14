package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled carbon report window. */
data class ScheduledCarbonReportWindowView(
    var codeText: String,
    var title: String,
    var contactEmail: String,
    var region: String,
    var graceDays: String,
    var rate: String,
    var dock: String,
    var distanceKm: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toScheduledCarbonReportWindowView(item: ScheduledCarbonReportWindow): ScheduledCarbonReportWindowView {
    return ScheduledCarbonReportWindowView(
        codeText = truncate(item.code, 80),
        title = item.title,
        contactEmail = truncate(item.contactEmail, 64),
        region = item.region,
        graceDays = item.graceDays.toString(),
        rate = "%.2f".format(item.rate),
        dock = item.dock,
        distanceKm = "%.2f".format(item.distanceKm),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCarbonReportWindowViews(items: MutableList<ScheduledCarbonReportWindow>): MutableList<ScheduledCarbonReportWindowView> {
    val views = mutableListOf<ScheduledCarbonReportWindowView>()
    for (item in items) {
        views.add(toScheduledCarbonReportWindowView(item))
    }
    return views
}

fun sumScheduledCarbonReportWindowView(view: ScheduledCarbonReportWindowView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedGraceDays.toDouble() + parsedRate + parsedDistanceKm
}
