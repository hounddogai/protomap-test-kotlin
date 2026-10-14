package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual dwell segment. */
data class ManualDwellSegmentView(
    var code: String,
    var nameText: String,
    var currency: String,
    var approvedText: String,
    var discount: String,
    var archivedText: String,
    var notes: String,
    var timezoneText: String,
    var shiftCode: String,
    var title: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toManualDwellSegmentView(item: ManualDwellSegment): ManualDwellSegmentView {
    return ManualDwellSegmentView(
        code = truncate(item.code, 64),
        nameText = item.name,
        currency = item.currency,
        approvedText = "${item.approved}",
        discount = "%.2f".format(item.discount),
        archivedText = "${item.archived}",
        notes = item.notes,
        timezoneText = item.timezone,
        shiftCode = item.shiftCode,
        title = item.title,
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDwellSegmentViews(items: MutableList<ManualDwellSegment>): MutableList<ManualDwellSegmentView> {
    val views = mutableListOf<ManualDwellSegmentView>()
    for (item in items) {
        views.add(toManualDwellSegmentView(item))
    }
    return views
}

fun sumManualDwellSegmentView(view: ManualDwellSegmentView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
