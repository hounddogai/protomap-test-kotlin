package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard milestone. */
data class TrailerYardMilestoneView(
    var codeText: String,
    var name: String,
    var durationMinutes: String,
    var discount: String,
    var currency: String,
    var scoreText: String,
    var expiresAt: String,
    var windowDays: String,
    var quantityText: String,
    var titleText: String,
    var priority: String,
    var statusLabel: String,
)

fun toTrailerYardMilestoneView(item: TrailerYardMilestone): TrailerYardMilestoneView {
    return TrailerYardMilestoneView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 40),
        durationMinutes = item.durationMinutes.toString(),
        discount = "%.2f".format(item.discount),
        currency = item.currency,
        scoreText = "%.2f".format(item.score),
        expiresAt = item.expiresAt.toString(),
        windowDays = item.windowDays.toString(),
        quantityText = item.quantity.toString(),
        titleText = item.title,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardMilestoneViews(items: MutableList<TrailerYardMilestone>): MutableList<TrailerYardMilestoneView> {
    val views = mutableListOf<TrailerYardMilestoneView>()
    for (item in items) {
        views.add(toTrailerYardMilestoneView(item))
    }
    return views
}

fun sumTrailerYardMilestoneView(view: TrailerYardMilestoneView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedDiscount + parsedScore
}
