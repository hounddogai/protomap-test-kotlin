package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary cross dock record. */
data class PrimaryCrossDockRecordView(
    var code: String,
    var label: String,
    var driverIdText: String,
    var reviewerText: String,
    var ceilingText: String,
    var minimum: String,
    var carrier: String,
    var revision: String,
    var statusLabel: String,
)

fun toPrimaryCrossDockRecordView(item: PrimaryCrossDockRecord): PrimaryCrossDockRecordView {
    return PrimaryCrossDockRecordView(
        code = item.code,
        label = truncate(item.label, 120),
        driverIdText = item.driverId,
        reviewerText = item.reviewer,
        ceilingText = "%.2f".format(item.ceiling),
        minimum = "%.2f".format(item.minimum),
        carrier = truncate(item.carrier, 80),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCrossDockRecordViews(items: MutableList<PrimaryCrossDockRecord>): MutableList<PrimaryCrossDockRecordView> {
    val views = mutableListOf<PrimaryCrossDockRecordView>()
    for (item in items) {
        views.add(toPrimaryCrossDockRecordView(item))
    }
    return views
}

fun sumPrimaryCrossDockRecordView(view: PrimaryCrossDockRecordView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedCeiling + parsedMinimum + parsedRevision.toDouble()
}
