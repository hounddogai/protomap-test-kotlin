package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled cutoff draft. */
data class ScheduledCutoffDraftView(
    var codeText: String,
    var label: String,
    var maximumText: String,
    var depot: String,
    var attempts: String,
    var country: String,
    var statusLabel: String,
)

fun toScheduledCutoffDraftView(item: ScheduledCutoffDraft): ScheduledCutoffDraftView {
    return ScheduledCutoffDraftView(
        codeText = item.code,
        label = item.label,
        maximumText = "%.2f".format(item.maximum),
        depot = truncate(item.depot, 64),
        attempts = item.attempts.toString(),
        country = truncate(item.country, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCutoffDraftViews(items: MutableList<ScheduledCutoffDraft>): MutableList<ScheduledCutoffDraftView> {
    val views = mutableListOf<ScheduledCutoffDraftView>()
    for (item in items) {
        views.add(toScheduledCutoffDraftView(item))
    }
    return views
}

fun sumScheduledCutoffDraftView(view: ScheduledCutoffDraftView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedMaximum + parsedAttempts.toDouble()
}
