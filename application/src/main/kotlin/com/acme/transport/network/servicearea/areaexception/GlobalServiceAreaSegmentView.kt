package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global service area segment. */
data class GlobalServiceAreaSegmentView(
    var code: String,
    var labelText: String,
    var notesText: String,
    var depot: String,
    var checksumText: String,
    var etaMinutes: String,
    var driverId: String,
    var ceilingText: String,
    var source: String,
    var statusLabel: String,
)

fun toGlobalServiceAreaSegmentView(item: GlobalServiceAreaSegment): GlobalServiceAreaSegmentView {
    return GlobalServiceAreaSegmentView(
        code = truncate(item.code, 64),
        labelText = item.label,
        notesText = truncate(item.notes, 120),
        depot = item.depot,
        checksumText = item.checksum,
        etaMinutes = item.etaMinutes.toString(),
        driverId = truncate(item.driverId, 64),
        ceilingText = "%.2f".format(item.ceiling),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalServiceAreaSegmentViews(items: MutableList<GlobalServiceAreaSegment>): MutableList<GlobalServiceAreaSegmentView> {
    val views = mutableListOf<GlobalServiceAreaSegmentView>()
    for (item in items) {
        views.add(toGlobalServiceAreaSegmentView(item))
    }
    return views
}

fun sumGlobalServiceAreaSegmentView(view: GlobalServiceAreaSegmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedCeiling
}
