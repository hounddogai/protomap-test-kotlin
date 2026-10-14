package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary defect target. */
data class PrimaryDefectTargetView(
    var codeText: String,
    var labelText: String,
    var floorValue: String,
    var maximum: String,
    var createdAt: String,
    var expiresAt: String,
    var shiftCode: String,
    var odometer: String,
    var lane: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toPrimaryDefectTargetView(item: PrimaryDefectTarget): PrimaryDefectTargetView {
    return PrimaryDefectTargetView(
        codeText = item.code,
        labelText = item.label,
        floorValue = "%.2f".format(item.floorValue),
        maximum = "%.2f".format(item.maximum),
        createdAt = item.createdAt.toString(),
        expiresAt = item.expiresAt.toString(),
        shiftCode = truncate(item.shiftCode, 80),
        odometer = item.odometer.toString(),
        lane = item.lane,
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDefectTargetViews(items: MutableList<PrimaryDefectTarget>): MutableList<PrimaryDefectTargetView> {
    val views = mutableListOf<PrimaryDefectTargetView>()
    for (item in items) {
        views.add(toPrimaryDefectTargetView(item))
    }
    return views
}

fun sumPrimaryDefectTargetView(view: PrimaryDefectTargetView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedFloorValue + parsedMaximum + parsedCreatedAt.toDouble()
}
