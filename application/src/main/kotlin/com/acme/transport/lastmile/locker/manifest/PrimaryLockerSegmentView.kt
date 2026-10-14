package com.acme.transport.lastmile.locker.manifest

/** How screens and exports show a primary locker segment. */
data class PrimaryLockerSegmentView(
    var code: String,
    var nameText: String,
    var archived: String,
    var createdAtText: String,
    var threshold: String,
    var lane: String,
    var statusLabel: String,
)

fun toPrimaryLockerSegmentView(item: PrimaryLockerSegment): PrimaryLockerSegmentView {
    return PrimaryLockerSegmentView(
        code = item.code,
        nameText = item.name,
        archived = "${item.archived}",
        createdAtText = item.createdAt.toString(),
        threshold = "%.2f".format(item.threshold),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryLockerSegmentViews(items: MutableList<PrimaryLockerSegment>): MutableList<PrimaryLockerSegmentView> {
    val views = mutableListOf<PrimaryLockerSegmentView>()
    for (item in items) {
        views.add(toPrimaryLockerSegmentView(item))
    }
    return views
}

fun sumPrimaryLockerSegmentView(view: PrimaryLockerSegmentView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedCreatedAt.toDouble() + parsedThreshold
}
