package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability segment. */
data class AvailabilitySegmentView(
    var code: String,
    var nameText: String,
    var attempts: String,
    var sourceText: String,
    var quantityText: String,
    var ratio: String,
    var ownerId: String,
    var pinnedText: String,
    var revision: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toAvailabilitySegmentView(item: AvailabilitySegment): AvailabilitySegmentView {
    return AvailabilitySegmentView(
        code = item.code,
        nameText = item.name,
        attempts = item.attempts.toString(),
        sourceText = truncate(item.source, 40),
        quantityText = item.quantity.toString(),
        ratio = "%.2f".format(item.ratio),
        ownerId = item.ownerId,
        pinnedText = "${item.pinned}",
        revision = item.revision.toString(),
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilitySegmentViews(items: MutableList<AvailabilitySegment>): MutableList<AvailabilitySegmentView> {
    val views = mutableListOf<AvailabilitySegmentView>()
    for (item in items) {
        views.add(toAvailabilitySegmentView(item))
    }
    return views
}

fun sumAvailabilitySegmentView(view: AvailabilitySegmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedAttempts.toDouble() + parsedQuantity.toDouble() + parsedRatio
}
