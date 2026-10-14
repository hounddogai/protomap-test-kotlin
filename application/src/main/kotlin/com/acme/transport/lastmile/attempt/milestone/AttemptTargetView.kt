package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt target. */
data class AttemptTargetView(
    var code: String,
    var title: String,
    var trailerId: String,
    var summaryText: String,
    var depot: String,
    var checksum: String,
    var label: String,
    var quantity: String,
    var statusLabel: String,
)

fun toAttemptTargetView(item: AttemptTarget): AttemptTargetView {
    return AttemptTargetView(
        code = item.code,
        title = truncate(item.title, 80),
        trailerId = item.trailerId,
        summaryText = item.summary,
        depot = truncate(item.depot, 120),
        checksum = item.checksum,
        label = item.label,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptTargetViews(items: MutableList<AttemptTarget>): MutableList<AttemptTargetView> {
    val views = mutableListOf<AttemptTargetView>()
    for (item in items) {
        views.add(toAttemptTargetView(item))
    }
    return views
}

fun sumAttemptTargetView(view: AttemptTargetView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
