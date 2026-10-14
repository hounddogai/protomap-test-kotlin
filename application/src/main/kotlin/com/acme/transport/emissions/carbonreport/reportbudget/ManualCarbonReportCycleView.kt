package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual carbon report cycle. */
data class ManualCarbonReportCycleView(
    var code: String,
    var title: String,
    var stopCount: String,
    var startsAt: String,
    var carrier: String,
    var versionText: String,
    var loadKgText: String,
    var driverId: String,
    var endsAtText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toManualCarbonReportCycleView(item: ManualCarbonReportCycle): ManualCarbonReportCycleView {
    return ManualCarbonReportCycleView(
        code = item.code,
        title = truncate(item.title, 64),
        stopCount = item.stopCount.toString(),
        startsAt = item.startsAt.toString(),
        carrier = truncate(item.carrier, 40),
        versionText = item.version.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        driverId = item.driverId,
        endsAtText = item.endsAt.toString(),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCarbonReportCycleViews(items: MutableList<ManualCarbonReportCycle>): MutableList<ManualCarbonReportCycleView> {
    val views = mutableListOf<ManualCarbonReportCycleView>()
    for (item in items) {
        views.add(toManualCarbonReportCycleView(item))
    }
    return views
}

fun sumManualCarbonReportCycleView(view: ManualCarbonReportCycleView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedStopCount.toDouble() + parsedStartsAt.toDouble() + parsedVersion.toDouble()
}
