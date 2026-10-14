package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict attempt claim. */
data class StrictAttemptClaimView(
    var codeText: String,
    var title: String,
    var notesText: String,
    var visible: String,
    var dock: String,
    var lane: String,
    var startsAtText: String,
    var depot: String,
    var statusLabel: String,
)

fun toStrictAttemptClaimView(item: StrictAttemptClaim): StrictAttemptClaimView {
    return StrictAttemptClaimView(
        codeText = item.code,
        title = item.title,
        notesText = truncate(item.notes, 40),
        visible = "${item.visible}",
        dock = item.dock,
        lane = truncate(item.lane, 40),
        startsAtText = item.startsAt.toString(),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAttemptClaimViews(items: MutableList<StrictAttemptClaim>): MutableList<StrictAttemptClaimView> {
    val views = mutableListOf<StrictAttemptClaimView>()
    for (item in items) {
        views.add(toStrictAttemptClaimView(item))
    }
    return views
}

fun sumStrictAttemptClaimView(view: StrictAttemptClaimView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedStartsAt.toDouble()
}
