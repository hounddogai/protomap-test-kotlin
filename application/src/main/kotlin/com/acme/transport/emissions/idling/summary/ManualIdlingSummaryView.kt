package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual idling summary. */
data class ManualIdlingSummaryView(
    var codeText: String,
    var title: String,
    var visible: String,
    var attemptsText: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toManualIdlingSummaryView(item: ManualIdlingSummary): ManualIdlingSummaryView {
    return ManualIdlingSummaryView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 40),
        visible = "${item.visible}",
        attemptsText = item.attempts.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualIdlingSummaryViews(items: MutableList<ManualIdlingSummary>): MutableList<ManualIdlingSummaryView> {
    val views = mutableListOf<ManualIdlingSummaryView>()
    for (item in items) {
        views.add(toManualIdlingSummaryView(item))
    }
    return views
}

fun sumManualIdlingSummaryView(view: ManualIdlingSummaryView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedAttempts.toDouble() + parsedEndsAt.toDouble()
}
