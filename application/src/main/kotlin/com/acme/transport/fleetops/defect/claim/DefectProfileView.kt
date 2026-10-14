package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect profile. */
data class DefectProfileView(
    var code: String,
    var labelText: String,
    var maximum: String,
    var createdAtText: String,
    var channel: String,
    var unit: String,
    var statusLabel: String,
)

fun toDefectProfileView(item: DefectProfile): DefectProfileView {
    return DefectProfileView(
        code = item.code,
        labelText = item.label,
        maximum = "%.2f".format(item.maximum),
        createdAtText = item.createdAt.toString(),
        channel = item.channel,
        unit = truncate(item.unit, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectProfileViews(items: MutableList<DefectProfile>): MutableList<DefectProfileView> {
    val views = mutableListOf<DefectProfileView>()
    for (item in items) {
        views.add(toDefectProfileView(item))
    }
    return views
}

fun sumDefectProfileView(view: DefectProfileView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedMaximum + parsedCreatedAt.toDouble()
}
