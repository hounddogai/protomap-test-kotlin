package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary camera event record. */
data class PrimaryCameraEventRecordView(
    var codeText: String,
    var title: String,
    var capacityText: String,
    var channel: String,
    var externalRefText: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toPrimaryCameraEventRecordView(item: PrimaryCameraEventRecord): PrimaryCameraEventRecordView {
    return PrimaryCameraEventRecordView(
        codeText = item.code,
        title = truncate(item.title, 40),
        capacityText = item.capacity.toString(),
        channel = truncate(item.channel, 40),
        externalRefText = item.externalRef,
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCameraEventRecordViews(items: MutableList<PrimaryCameraEventRecord>): MutableList<PrimaryCameraEventRecordView> {
    val views = mutableListOf<PrimaryCameraEventRecordView>()
    for (item in items) {
        views.add(toPrimaryCameraEventRecordView(item))
    }
    return views
}

fun sumPrimaryCameraEventRecordView(view: PrimaryCameraEventRecordView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedCapacity.toDouble() + parsedOdometer.toDouble()
}
