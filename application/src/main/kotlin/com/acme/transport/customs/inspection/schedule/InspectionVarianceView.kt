package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection variance. */
data class InspectionVarianceView(
    var code: String,
    var name: String,
    var localeText: String,
    var cost: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toInspectionVarianceView(item: InspectionVariance): InspectionVarianceView {
    return InspectionVarianceView(
        code = truncate(item.code, 120),
        name = item.name,
        localeText = item.locale,
        cost = "%.2f".format(item.cost),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionVarianceViews(items: MutableList<InspectionVariance>): MutableList<InspectionVarianceView> {
    val views = mutableListOf<InspectionVarianceView>()
    for (item in items) {
        views.add(toInspectionVarianceView(item))
    }
    return views
}

fun sumInspectionVarianceView(view: InspectionVarianceView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedCost + parsedEtaMinutes.toDouble()
}
