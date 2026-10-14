package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification slot. */
data class QualificationSlotView(
    var code: String,
    var title: String,
    var driverId: String,
    var unit: String,
    var country: String,
    var visible: String,
    var margin: String,
    var ownerId: String,
    var labelText: String,
    var channel: String,
    var capacity: String,
    var statusLabel: String,
)

fun toQualificationSlotView(item: QualificationSlot): QualificationSlotView {
    return QualificationSlotView(
        code = item.code,
        title = item.title,
        driverId = truncate(item.driverId, 80),
        unit = truncate(item.unit, 40),
        country = item.country,
        visible = "${item.visible}",
        margin = "%.2f".format(item.margin),
        ownerId = item.ownerId,
        labelText = item.label,
        channel = item.channel,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationSlotViews(items: MutableList<QualificationSlot>): MutableList<QualificationSlotView> {
    val views = mutableListOf<QualificationSlotView>()
    for (item in items) {
        views.add(toQualificationSlotView(item))
    }
    return views
}

fun sumQualificationSlotView(view: QualificationSlotView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedMargin + parsedCapacity.toDouble()
}
