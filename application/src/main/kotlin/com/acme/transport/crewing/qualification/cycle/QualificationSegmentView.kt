package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification segment. */
data class QualificationSegmentView(
    var code: String,
    var name: String,
    var version: String,
    var lane: String,
    var routeCodeText: String,
    var regionText: String,
    var pinned: String,
    var titleText: String,
    var pallets: String,
    var maximum: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toQualificationSegmentView(item: QualificationSegment): QualificationSegmentView {
    return QualificationSegmentView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 40),
        version = item.version.toString(),
        lane = item.lane,
        routeCodeText = item.routeCode,
        regionText = item.region,
        pinned = "${item.pinned}",
        titleText = item.title,
        pallets = item.pallets.toString(),
        maximum = "%.2f".format(item.maximum),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationSegmentViews(items: MutableList<QualificationSegment>): MutableList<QualificationSegmentView> {
    val views = mutableListOf<QualificationSegmentView>()
    for (item in items) {
        views.add(toQualificationSegmentView(item))
    }
    return views
}

fun sumQualificationSegmentView(view: QualificationSegmentView): Double {
    val parsedVersion = view.version.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedVersion.toDouble() + parsedPallets.toDouble() + parsedMaximum
}
