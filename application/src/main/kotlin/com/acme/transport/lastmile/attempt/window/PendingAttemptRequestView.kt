package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending attempt request. */
data class PendingAttemptRequestView(
    var codeText: String,
    var labelText: String,
    var attempts: String,
    var windowDaysText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toPendingAttemptRequestView(item: PendingAttemptRequest): PendingAttemptRequestView {
    return PendingAttemptRequestView(
        codeText = item.code,
        labelText = item.label,
        attempts = item.attempts.toString(),
        windowDaysText = item.windowDays.toString(),
        trailerId = truncate(item.trailerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAttemptRequestViews(items: MutableList<PendingAttemptRequest>): MutableList<PendingAttemptRequestView> {
    val views = mutableListOf<PendingAttemptRequestView>()
    for (item in items) {
        views.add(toPendingAttemptRequestView(item))
    }
    return views
}

fun sumPendingAttemptRequestView(view: PendingAttemptRequestView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedAttempts.toDouble() + parsedWindowDays.toDouble()
}
