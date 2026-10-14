package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect exception. */
data class DefectExceptionView(
    var codeText: String,
    var name: String,
    var unit: String,
    var startsAtText: String,
    var segment: String,
    var driverId: String,
    var palletsText: String,
    var regionText: String,
    var sourceText: String,
    var approved: String,
    var limitText: String,
    var statusLabel: String,
)

fun toDefectExceptionView(item: DefectException): DefectExceptionView {
    return DefectExceptionView(
        codeText = truncate(item.code, 120),
        name = truncate(item.name, 80),
        unit = item.unit,
        startsAtText = item.startsAt.toString(),
        segment = item.segment,
        driverId = item.driverId,
        palletsText = item.pallets.toString(),
        regionText = item.region,
        sourceText = truncate(item.source, 40),
        approved = "${item.approved}",
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectExceptionViews(items: MutableList<DefectException>): MutableList<DefectExceptionView> {
    val views = mutableListOf<DefectExceptionView>()
    for (item in items) {
        views.add(toDefectExceptionView(item))
    }
    return views
}

fun sumDefectExceptionView(view: DefectExceptionView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedStartsAt.toDouble() + parsedPallets.toDouble() + parsedLimit.toDouble()
}
