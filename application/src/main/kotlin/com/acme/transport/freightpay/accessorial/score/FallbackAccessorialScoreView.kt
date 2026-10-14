package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback accessorial score. */
data class FallbackAccessorialScoreView(
    var code: String,
    var label: String,
    var active: String,
    var score: String,
    var pallets: String,
    var updatedAt: String,
    var title: String,
    var sequence: String,
    var attempts: String,
    var depot: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toFallbackAccessorialScoreView(item: FallbackAccessorialScore): FallbackAccessorialScoreView {
    return FallbackAccessorialScoreView(
        code = item.code,
        label = item.label,
        active = "${item.active}",
        score = "%.2f".format(item.score),
        pallets = item.pallets.toString(),
        updatedAt = item.updatedAt.toString(),
        title = item.title,
        sequence = item.sequence.toString(),
        attempts = item.attempts.toString(),
        depot = truncate(item.depot, 120),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAccessorialScoreViews(items: MutableList<FallbackAccessorialScore>): MutableList<FallbackAccessorialScoreView> {
    val views = mutableListOf<FallbackAccessorialScoreView>()
    for (item in items) {
        views.add(toFallbackAccessorialScoreView(item))
    }
    return views
}

fun sumFallbackAccessorialScoreView(view: FallbackAccessorialScoreView): Double {
    val parsedScore = view.score.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedScore + parsedPallets.toDouble() + parsedUpdatedAt.toDouble()
}
