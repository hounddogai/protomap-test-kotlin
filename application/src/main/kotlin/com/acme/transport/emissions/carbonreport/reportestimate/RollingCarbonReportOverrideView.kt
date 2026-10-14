package com.acme.transport.emissions.carbonreport.reportestimate

/** How screens and exports show a rolling carbon report override. */
data class RollingCarbonReportOverrideView(
    var code: String,
    var labelText: String,
    var sequence: String,
    var etaMinutes: String,
    var windowDaysText: String,
    var categoryText: String,
    var timezone: String,
    var margin: String,
    var statusLabel: String,
)

fun toRollingCarbonReportOverrideView(item: RollingCarbonReportOverride): RollingCarbonReportOverrideView {
    return RollingCarbonReportOverrideView(
        code = item.code,
        labelText = item.label,
        sequence = item.sequence.toString(),
        etaMinutes = item.etaMinutes.toString(),
        windowDaysText = item.windowDays.toString(),
        categoryText = item.category,
        timezone = item.timezone,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCarbonReportOverrideViews(items: MutableList<RollingCarbonReportOverride>): MutableList<RollingCarbonReportOverrideView> {
    val views = mutableListOf<RollingCarbonReportOverrideView>()
    for (item in items) {
        views.add(toRollingCarbonReportOverrideView(item))
    }
    return views
}

fun sumRollingCarbonReportOverrideView(view: RollingCarbonReportOverrideView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedSequence.toDouble() + parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble()
}
