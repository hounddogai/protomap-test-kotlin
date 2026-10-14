package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary tender segment. */
data class PrimaryTenderSegmentView(
    var code: String,
    var title: String,
    var discountText: String,
    var lane: String,
    var floorValue: String,
    var depot: String,
    var approved: String,
    var updatedAt: String,
    var durationMinutes: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toPrimaryTenderSegmentView(item: PrimaryTenderSegment): PrimaryTenderSegmentView {
    return PrimaryTenderSegmentView(
        code = truncate(item.code, 40),
        title = item.title,
        discountText = "%.2f".format(item.discount),
        lane = truncate(item.lane, 64),
        floorValue = "%.2f".format(item.floorValue),
        depot = item.depot,
        approved = "${item.approved}",
        updatedAt = item.updatedAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTenderSegmentViews(items: MutableList<PrimaryTenderSegment>): MutableList<PrimaryTenderSegmentView> {
    val views = mutableListOf<PrimaryTenderSegmentView>()
    for (item in items) {
        views.add(toPrimaryTenderSegmentView(item))
    }
    return views
}

fun sumPrimaryTenderSegmentView(view: PrimaryTenderSegmentView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedDiscount + parsedFloorValue + parsedUpdatedAt.toDouble()
}
