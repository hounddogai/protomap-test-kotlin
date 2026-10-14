package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dock door batch. */
data class StrictDockDoorBatchView(
    var code: String,
    var titleText: String,
    var descriptionText: String,
    var notes: String,
    var amount: String,
    var statusLabel: String,
)

fun toStrictDockDoorBatchView(item: StrictDockDoorBatch): StrictDockDoorBatchView {
    return StrictDockDoorBatchView(
        code = truncate(item.code, 120),
        titleText = item.title,
        descriptionText = item.description,
        notes = item.notes,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDockDoorBatchViews(items: MutableList<StrictDockDoorBatch>): MutableList<StrictDockDoorBatchView> {
    val views = mutableListOf<StrictDockDoorBatchView>()
    for (item in items) {
        views.add(toStrictDockDoorBatchView(item))
    }
    return views
}

fun sumStrictDockDoorBatchView(view: StrictDockDoorBatchView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
