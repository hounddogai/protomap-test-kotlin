package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback declaration adjustment. */
data class FallbackDeclarationAdjustmentView(
    var code: String,
    var nameText: String,
    var carrier: String,
    var score: String,
    var ceiling: String,
    var summary: String,
    var endsAtText: String,
    var ownerIdText: String,
    var shiftCode: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toFallbackDeclarationAdjustmentView(item: FallbackDeclarationAdjustment): FallbackDeclarationAdjustmentView {
    return FallbackDeclarationAdjustmentView(
        code = item.code,
        nameText = item.name,
        carrier = item.carrier,
        score = "%.2f".format(item.score),
        ceiling = "%.2f".format(item.ceiling),
        summary = item.summary,
        endsAtText = item.endsAt.toString(),
        ownerIdText = truncate(item.ownerId, 64),
        shiftCode = truncate(item.shiftCode, 120),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDeclarationAdjustmentViews(items: MutableList<FallbackDeclarationAdjustment>): MutableList<FallbackDeclarationAdjustmentView> {
    val views = mutableListOf<FallbackDeclarationAdjustmentView>()
    for (item in items) {
        views.add(toFallbackDeclarationAdjustmentView(item))
    }
    return views
}

fun sumFallbackDeclarationAdjustmentView(view: FallbackDeclarationAdjustmentView): Double {
    val parsedScore = view.score.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedScore + parsedCeiling + parsedEndsAt.toDouble()
}
