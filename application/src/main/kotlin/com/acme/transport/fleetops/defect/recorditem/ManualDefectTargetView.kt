package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual defect target. */
data class ManualDefectTargetView(
    var code: String,
    var label: String,
    var ownerId: String,
    var quantityText: String,
    var score: String,
    var active: String,
    var statusLabel: String,
)

fun toManualDefectTargetView(item: ManualDefectTarget): ManualDefectTargetView {
    return ManualDefectTargetView(
        code = item.code,
        label = item.label,
        ownerId = truncate(item.ownerId, 64),
        quantityText = item.quantity.toString(),
        score = "%.2f".format(item.score),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDefectTargetViews(items: MutableList<ManualDefectTarget>): MutableList<ManualDefectTargetView> {
    val views = mutableListOf<ManualDefectTargetView>()
    for (item in items) {
        views.add(toManualDefectTargetView(item))
    }
    return views
}

fun sumManualDefectTargetView(view: ManualDefectTargetView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedQuantity.toDouble() + parsedScore
}
