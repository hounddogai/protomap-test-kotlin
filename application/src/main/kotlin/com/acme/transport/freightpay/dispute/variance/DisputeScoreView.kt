package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute score. */
data class DisputeScoreView(
    var code: String,
    var title: String,
    var timezone: String,
    var notesText: String,
    var endsAtText: String,
    var createdAtText: String,
    var unit: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDisputeScoreView(item: DisputeScore): DisputeScoreView {
    return DisputeScoreView(
        code = item.code,
        title = item.title,
        timezone = truncate(item.timezone, 120),
        notesText = item.notes,
        endsAtText = item.endsAt.toString(),
        createdAtText = item.createdAt.toString(),
        unit = item.unit,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeScoreViews(items: MutableList<DisputeScore>): MutableList<DisputeScoreView> {
    val views = mutableListOf<DisputeScoreView>()
    for (item in items) {
        views.add(toDisputeScoreView(item))
    }
    return views
}

fun sumDisputeScoreView(view: DisputeScoreView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedEndsAt.toDouble() + parsedCreatedAt.toDouble() + parsedQuantity.toDouble()
}
