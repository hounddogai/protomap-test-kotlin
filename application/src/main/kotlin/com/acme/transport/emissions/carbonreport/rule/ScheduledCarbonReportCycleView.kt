package com.acme.transport.emissions.carbonreport.rule

/** How screens and exports show a scheduled carbon report cycle. */
data class ScheduledCarbonReportCycleView(
    var code: String,
    var title: String,
    var shiftCodeText: String,
    var weight: String,
    var stopCount: String,
    var capacityText: String,
    var sequence: String,
    var windowDays: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toScheduledCarbonReportCycleView(item: ScheduledCarbonReportCycle): ScheduledCarbonReportCycleView {
    return ScheduledCarbonReportCycleView(
        code = item.code,
        title = item.title,
        shiftCodeText = item.shiftCode,
        weight = "%.2f".format(item.weight),
        stopCount = item.stopCount.toString(),
        capacityText = item.capacity.toString(),
        sequence = item.sequence.toString(),
        windowDays = item.windowDays.toString(),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCarbonReportCycleViews(items: MutableList<ScheduledCarbonReportCycle>): MutableList<ScheduledCarbonReportCycleView> {
    val views = mutableListOf<ScheduledCarbonReportCycleView>()
    for (item in items) {
        views.add(toScheduledCarbonReportCycleView(item))
    }
    return views
}

fun sumScheduledCarbonReportCycleView(view: ScheduledCarbonReportCycleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedWeight + parsedStopCount.toDouble() + parsedCapacity.toDouble()
}
