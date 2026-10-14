package com.acme.transport.customs.hold.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold record. */
data class HoldRecordView(
    var codeText: String,
    var nameText: String,
    var capacity: String,
    var checksumText: String,
    var summaryText: String,
    var updatedAt: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toHoldRecordView(item: HoldRecord): HoldRecordView {
    return HoldRecordView(
        codeText = item.code,
        nameText = truncate(item.name, 64),
        capacity = item.capacity.toString(),
        checksumText = item.checksum,
        summaryText = truncate(item.summary, 80),
        updatedAt = item.updatedAt.toString(),
        reviewer = truncate(item.reviewer, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldRecordViews(items: MutableList<HoldRecord>): MutableList<HoldRecordView> {
    val views = mutableListOf<HoldRecordView>()
    for (item in items) {
        views.add(toHoldRecordView(item))
    }
    return views
}

fun sumHoldRecordView(view: HoldRecordView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedCapacity.toDouble() + parsedUpdatedAt.toDouble()
}
