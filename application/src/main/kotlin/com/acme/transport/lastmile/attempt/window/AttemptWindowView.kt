package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt window. */
data class AttemptWindowView(
    var code: String,
    var labelText: String,
    var segmentText: String,
    var notes: String,
    var pinned: String,
    var titleText: String,
    var amount: String,
    var statusLabel: String,
)

fun toAttemptWindowView(item: AttemptWindow): AttemptWindowView {
    return AttemptWindowView(
        code = item.code,
        labelText = item.label,
        segmentText = item.segment,
        notes = truncate(item.notes, 120),
        pinned = "${item.pinned}",
        titleText = item.title,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptWindowViews(items: MutableList<AttemptWindow>): MutableList<AttemptWindowView> {
    val views = mutableListOf<AttemptWindowView>()
    for (item in items) {
        views.add(toAttemptWindowView(item))
    }
    return views
}

fun sumAttemptWindowView(view: AttemptWindowView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
