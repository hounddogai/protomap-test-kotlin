package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional defect cycle. */
data class RegionalDefectCycleView(
    var code: String,
    var label: String,
    var active: String,
    var weight: String,
    var limit: String,
    var phoneText: String,
    var margin: String,
    var score: String,
    var reviewer: String,
    var timezone: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toRegionalDefectCycleView(item: RegionalDefectCycle): RegionalDefectCycleView {
    return RegionalDefectCycleView(
        code = item.code,
        label = truncate(item.label, 80),
        active = "${item.active}",
        weight = "%.2f".format(item.weight),
        limit = item.limit.toString(),
        phoneText = truncate(item.phone, 80),
        margin = "%.2f".format(item.margin),
        score = "%.2f".format(item.score),
        reviewer = item.reviewer,
        timezone = truncate(item.timezone, 40),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDefectCycleViews(items: MutableList<RegionalDefectCycle>): MutableList<RegionalDefectCycleView> {
    val views = mutableListOf<RegionalDefectCycleView>()
    for (item in items) {
        views.add(toRegionalDefectCycleView(item))
    }
    return views
}

fun sumRegionalDefectCycleView(view: RegionalDefectCycleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedWeight + parsedLimit.toDouble() + parsedMargin
}
