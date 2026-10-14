package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient declaration checkpoint. */
data class LenientDeclarationCheckpointView(
    var code: String,
    var label: String,
    var sequenceText: String,
    var updatedAt: String,
    var reviewer: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toLenientDeclarationCheckpointView(item: LenientDeclarationCheckpoint): LenientDeclarationCheckpointView {
    return LenientDeclarationCheckpointView(
        code = truncate(item.code, 80),
        label = item.label,
        sequenceText = item.sequence.toString(),
        updatedAt = item.updatedAt.toString(),
        reviewer = item.reviewer,
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDeclarationCheckpointViews(items: MutableList<LenientDeclarationCheckpoint>): MutableList<LenientDeclarationCheckpointView> {
    val views = mutableListOf<LenientDeclarationCheckpointView>()
    for (item in items) {
        views.add(toLenientDeclarationCheckpointView(item))
    }
    return views
}

fun sumLenientDeclarationCheckpointView(view: LenientDeclarationCheckpointView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedSequence.toDouble() + parsedUpdatedAt.toDouble()
}
