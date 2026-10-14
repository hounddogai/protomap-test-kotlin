package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped locker segment. */
data class CappedLockerSegmentView(
    var code: String,
    var labelText: String,
    var ceiling: String,
    var segmentText: String,
    var channelText: String,
    var statusLabel: String,
)

fun toCappedLockerSegmentView(item: CappedLockerSegment): CappedLockerSegmentView {
    return CappedLockerSegmentView(
        code = item.code,
        labelText = item.label,
        ceiling = "%.2f".format(item.ceiling),
        segmentText = truncate(item.segment, 80),
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLockerSegmentViews(items: MutableList<CappedLockerSegment>): MutableList<CappedLockerSegmentView> {
    val views = mutableListOf<CappedLockerSegmentView>()
    for (item in items) {
        views.add(toCappedLockerSegmentView(item))
    }
    return views
}

fun sumCappedLockerSegmentView(view: CappedLockerSegmentView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}
