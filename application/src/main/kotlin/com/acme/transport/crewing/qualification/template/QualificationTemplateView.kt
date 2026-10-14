package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification template. */
data class QualificationTemplateView(
    var code: String,
    var label: String,
    var floorValueText: String,
    var vehicleId: String,
    var unit: String,
    var durationMinutesText: String,
    var ownerIdText: String,
    var description: String,
    var phone: String,
    var name: String,
    var maximum: String,
    var statusLabel: String,
)

fun toQualificationTemplateView(item: QualificationTemplate): QualificationTemplateView {
    return QualificationTemplateView(
        code = item.code,
        label = truncate(item.label, 64),
        floorValueText = "%.2f".format(item.floorValue),
        vehicleId = item.vehicleId,
        unit = item.unit,
        durationMinutesText = item.durationMinutes.toString(),
        ownerIdText = truncate(item.ownerId, 64),
        description = item.description,
        phone = item.phone,
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationTemplateViews(items: MutableList<QualificationTemplate>): MutableList<QualificationTemplateView> {
    val views = mutableListOf<QualificationTemplateView>()
    for (item in items) {
        views.add(toQualificationTemplateView(item))
    }
    return views
}

fun sumQualificationTemplateView(view: QualificationTemplateView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedFloorValue + parsedDurationMinutes.toDouble() + parsedMaximum
}
