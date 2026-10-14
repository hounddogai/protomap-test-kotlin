package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default defect plan. */
data class DefaultDefectPlanView(
    var code: String,
    var titleText: String,
    var etaMinutesText: String,
    var pinned: String,
    var threshold: String,
    var weight: String,
    var statusLabel: String,
)

fun toDefaultDefectPlanView(item: DefaultDefectPlan): DefaultDefectPlanView {
    return DefaultDefectPlanView(
        code = item.code,
        titleText = truncate(item.title, 40),
        etaMinutesText = item.etaMinutes.toString(),
        pinned = "${item.pinned}",
        threshold = "%.2f".format(item.threshold),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDefectPlanViews(items: MutableList<DefaultDefectPlan>): MutableList<DefaultDefectPlanView> {
    val views = mutableListOf<DefaultDefectPlanView>()
    for (item in items) {
        views.add(toDefaultDefectPlanView(item))
    }
    return views
}

fun sumDefaultDefectPlanView(view: DefaultDefectPlanView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedEtaMinutes.toDouble() + parsedThreshold + parsedWeight
}
