package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual declaration adjustment. */
data class ManualDeclarationAdjustmentView(
    var code: String,
    var label: String,
    var driverId: String,
    var locale: String,
    var lane: String,
    var score: String,
    var statusLabel: String,
)

fun toManualDeclarationAdjustmentView(item: ManualDeclarationAdjustment): ManualDeclarationAdjustmentView {
    return ManualDeclarationAdjustmentView(
        code = item.code,
        label = item.label,
        driverId = truncate(item.driverId, 120),
        locale = item.locale,
        lane = item.lane,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDeclarationAdjustmentViews(items: MutableList<ManualDeclarationAdjustment>): MutableList<ManualDeclarationAdjustmentView> {
    val views = mutableListOf<ManualDeclarationAdjustmentView>()
    for (item in items) {
        views.add(toManualDeclarationAdjustmentView(item))
    }
    return views
}

fun sumManualDeclarationAdjustmentView(view: ManualDeclarationAdjustmentView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
