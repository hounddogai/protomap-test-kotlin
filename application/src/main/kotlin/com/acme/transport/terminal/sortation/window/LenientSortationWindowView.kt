package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient sortation window. */
data class LenientSortationWindowView(
    var code: String,
    var name: String,
    var notesText: String,
    var sequenceText: String,
    var ownerId: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toLenientSortationWindowView(item: LenientSortationWindow): LenientSortationWindowView {
    return LenientSortationWindowView(
        code = item.code,
        name = item.name,
        notesText = item.notes,
        sequenceText = item.sequence.toString(),
        ownerId = truncate(item.ownerId, 64),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientSortationWindowViews(items: MutableList<LenientSortationWindow>): MutableList<LenientSortationWindowView> {
    val views = mutableListOf<LenientSortationWindowView>()
    for (item in items) {
        views.add(toLenientSortationWindowView(item))
    }
    return views
}

fun sumLenientSortationWindowView(view: LenientSortationWindowView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedSequence.toDouble() + parsedBatchSize.toDouble()
}
