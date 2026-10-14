package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt tier. */
data class AttemptTierView(
    var codeText: String,
    var label: String,
    var etaMinutesText: String,
    var approved: String,
    var locale: String,
    var pinned: String,
    var reviewer: String,
    var batchSize: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toAttemptTierView(item: AttemptTier): AttemptTierView {
    return AttemptTierView(
        codeText = truncate(item.code, 80),
        label = item.label,
        etaMinutesText = item.etaMinutes.toString(),
        approved = "${item.approved}",
        locale = item.locale,
        pinned = "${item.pinned}",
        reviewer = item.reviewer,
        batchSize = item.batchSize.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptTierViews(items: MutableList<AttemptTier>): MutableList<AttemptTierView> {
    val views = mutableListOf<AttemptTierView>()
    for (item in items) {
        views.add(toAttemptTierView(item))
    }
    return views
}

fun sumAttemptTierView(view: AttemptTierView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEtaMinutes.toDouble() + parsedBatchSize.toDouble() + parsedEndsAt.toDouble()
}
