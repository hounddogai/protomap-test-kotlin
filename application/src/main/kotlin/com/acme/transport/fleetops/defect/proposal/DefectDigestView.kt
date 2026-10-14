package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect digest. */
data class DefectDigestView(
    var code: String,
    var label: String,
    var odometer: String,
    var ratio: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toDefectDigestView(item: DefectDigest): DefectDigestView {
    return DefectDigestView(
        code = item.code,
        label = truncate(item.label, 80),
        odometer = item.odometer.toString(),
        ratio = "%.2f".format(item.ratio),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectDigestViews(items: MutableList<DefectDigest>): MutableList<DefectDigestView> {
    val views = mutableListOf<DefectDigestView>()
    for (item in items) {
        views.add(toDefectDigestView(item))
    }
    return views
}

fun sumDefectDigestView(view: DefectDigestView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedOdometer.toDouble() + parsedRatio + parsedStartsAt.toDouble()
}
