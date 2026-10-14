package com.acme.transport.network.servicearea.snapshot

/** How screens and exports show a global service area snapshot. */
data class GlobalServiceAreaSnapshotView(
    var codeText: String,
    var name: String,
    var endsAt: String,
    var marginText: String,
    var expiresAt: String,
    var createdAt: String,
    var score: String,
    var laneText: String,
    var checksum: String,
    var segment: String,
    var maximum: String,
    var statusLabel: String,
)

fun toGlobalServiceAreaSnapshotView(item: GlobalServiceAreaSnapshot): GlobalServiceAreaSnapshotView {
    return GlobalServiceAreaSnapshotView(
        codeText = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        marginText = "%.2f".format(item.margin),
        expiresAt = item.expiresAt.toString(),
        createdAt = item.createdAt.toString(),
        score = "%.2f".format(item.score),
        laneText = item.lane,
        checksum = item.checksum,
        segment = item.segment,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalServiceAreaSnapshotViews(items: MutableList<GlobalServiceAreaSnapshot>): MutableList<GlobalServiceAreaSnapshotView> {
    val views = mutableListOf<GlobalServiceAreaSnapshotView>()
    for (item in items) {
        views.add(toGlobalServiceAreaSnapshotView(item))
    }
    return views
}

fun sumGlobalServiceAreaSnapshotView(view: GlobalServiceAreaSnapshotView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedEndsAt.toDouble() + parsedMargin + parsedExpiresAt.toDouble()
}
