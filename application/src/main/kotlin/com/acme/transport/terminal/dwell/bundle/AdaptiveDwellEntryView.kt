package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive dwell entry. */
data class AdaptiveDwellEntryView(
    var code: String,
    var nameText: String,
    var externalRef: String,
    var startsAt: String,
    var pinnedText: String,
    var updatedAt: String,
    var reviewer: String,
    var capacityText: String,
    var stopCount: String,
    var limitText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toAdaptiveDwellEntryView(item: AdaptiveDwellEntry): AdaptiveDwellEntryView {
    return AdaptiveDwellEntryView(
        code = truncate(item.code, 40),
        nameText = item.name,
        externalRef = item.externalRef,
        startsAt = item.startsAt.toString(),
        pinnedText = "${item.pinned}",
        updatedAt = item.updatedAt.toString(),
        reviewer = item.reviewer,
        capacityText = item.capacity.toString(),
        stopCount = item.stopCount.toString(),
        limitText = item.limit.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDwellEntryViews(items: MutableList<AdaptiveDwellEntry>): MutableList<AdaptiveDwellEntryView> {
    val views = mutableListOf<AdaptiveDwellEntryView>()
    for (item in items) {
        views.add(toAdaptiveDwellEntryView(item))
    }
    return views
}

fun sumAdaptiveDwellEntryView(view: AdaptiveDwellEntryView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedStartsAt.toDouble() + parsedUpdatedAt.toDouble() + parsedCapacity.toDouble()
}
