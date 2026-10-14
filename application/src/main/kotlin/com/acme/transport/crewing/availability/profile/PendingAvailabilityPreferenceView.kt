package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending availability preference. */
data class PendingAvailabilityPreferenceView(
    var codeText: String,
    var label: String,
    var updatedAtText: String,
    var depot: String,
    var windowDays: String,
    var segment: String,
    var source: String,
    var unitText: String,
    var description: String,
    var externalRef: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toPendingAvailabilityPreferenceView(item: PendingAvailabilityPreference): PendingAvailabilityPreferenceView {
    return PendingAvailabilityPreferenceView(
        codeText = truncate(item.code, 64),
        label = item.label,
        updatedAtText = item.updatedAt.toString(),
        depot = item.depot,
        windowDays = item.windowDays.toString(),
        segment = item.segment,
        source = item.source,
        unitText = item.unit,
        description = truncate(item.description, 80),
        externalRef = item.externalRef,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAvailabilityPreferenceViews(items: MutableList<PendingAvailabilityPreference>): MutableList<PendingAvailabilityPreferenceView> {
    val views = mutableListOf<PendingAvailabilityPreferenceView>()
    for (item in items) {
        views.add(toPendingAvailabilityPreferenceView(item))
    }
    return views
}

fun sumPendingAvailabilityPreferenceView(view: PendingAvailabilityPreferenceView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedWindowDays.toDouble() + parsedScore
}
