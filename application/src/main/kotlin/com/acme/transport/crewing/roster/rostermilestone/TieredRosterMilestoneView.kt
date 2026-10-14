package com.acme.transport.crewing.roster.rostermilestone

/** How screens and exports show a tiered roster milestone. */
data class TieredRosterMilestoneView(
    var code: String,
    var title: String,
    var notesText: String,
    var sequenceText: String,
    var expiresAt: String,
    var scoreText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toTieredRosterMilestoneView(item: TieredRosterMilestone): TieredRosterMilestoneView {
    return TieredRosterMilestoneView(
        code = item.code,
        title = item.title,
        notesText = item.notes,
        sequenceText = item.sequence.toString(),
        expiresAt = item.expiresAt.toString(),
        scoreText = "%.2f".format(item.score),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRosterMilestoneViews(items: MutableList<TieredRosterMilestone>): MutableList<TieredRosterMilestoneView> {
    val views = mutableListOf<TieredRosterMilestoneView>()
    for (item in items) {
        views.add(toTieredRosterMilestoneView(item))
    }
    return views
}

fun sumTieredRosterMilestoneView(view: TieredRosterMilestoneView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedSequence.toDouble() + parsedExpiresAt.toDouble() + parsedScore
}
