package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy accessorial draft. */
data class LegacyAccessorialDraftView(
    var code: String,
    var title: String,
    var notes: String,
    var threshold: String,
    var durationMinutes: String,
    var stopCount: String,
    var sequence: String,
    var statusLabel: String,
)

fun toLegacyAccessorialDraftView(item: LegacyAccessorialDraft): LegacyAccessorialDraftView {
    return LegacyAccessorialDraftView(
        code = item.code,
        title = truncate(item.title, 120),
        notes = item.notes,
        threshold = "%.2f".format(item.threshold),
        durationMinutes = item.durationMinutes.toString(),
        stopCount = item.stopCount.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyAccessorialDraftViews(items: MutableList<LegacyAccessorialDraft>): MutableList<LegacyAccessorialDraftView> {
    val views = mutableListOf<LegacyAccessorialDraftView>()
    for (item in items) {
        views.add(toLegacyAccessorialDraftView(item))
    }
    return views
}

fun sumLegacyAccessorialDraftView(view: LegacyAccessorialDraftView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedThreshold + parsedDurationMinutes.toDouble() + parsedStopCount.toDouble()
}
