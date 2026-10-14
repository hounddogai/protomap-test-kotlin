package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dwell assignment. */
data class PendingDwellAssignmentView(
    var codeText: String,
    var title: String,
    var country: String,
    var quantityText: String,
    var loadKg: String,
    var sequence: String,
    var palletsText: String,
    var dock: String,
    var statusLabel: String,
)

fun toPendingDwellAssignmentView(item: PendingDwellAssignment): PendingDwellAssignmentView {
    return PendingDwellAssignmentView(
        codeText = item.code,
        title = item.title,
        country = truncate(item.country, 40),
        quantityText = item.quantity.toString(),
        loadKg = "%.2f".format(item.loadKg),
        sequence = item.sequence.toString(),
        palletsText = item.pallets.toString(),
        dock = truncate(item.dock, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDwellAssignmentViews(items: MutableList<PendingDwellAssignment>): MutableList<PendingDwellAssignmentView> {
    val views = mutableListOf<PendingDwellAssignmentView>()
    for (item in items) {
        views.add(toPendingDwellAssignmentView(item))
    }
    return views
}

fun sumPendingDwellAssignmentView(view: PendingDwellAssignmentView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedQuantity.toDouble() + parsedLoadKg + parsedSequence.toDouble()
}
