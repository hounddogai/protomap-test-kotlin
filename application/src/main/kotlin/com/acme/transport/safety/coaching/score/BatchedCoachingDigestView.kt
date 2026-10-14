package com.acme.transport.safety.coaching.score

/** How screens and exports show a batched coaching digest. */
data class BatchedCoachingDigestView(
    var code: String,
    var nameText: String,
    var floorValue: String,
    var startsAt: String,
    var loadKg: String,
    var visible: String,
    var routeCode: String,
    var notes: String,
    var statusLabel: String,
)

fun toBatchedCoachingDigestView(item: BatchedCoachingDigest): BatchedCoachingDigestView {
    return BatchedCoachingDigestView(
        code = item.code,
        nameText = item.name,
        floorValue = "%.2f".format(item.floorValue),
        startsAt = item.startsAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        visible = "${item.visible}",
        routeCode = item.routeCode,
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCoachingDigestViews(items: MutableList<BatchedCoachingDigest>): MutableList<BatchedCoachingDigestView> {
    val views = mutableListOf<BatchedCoachingDigestView>()
    for (item in items) {
        views.add(toBatchedCoachingDigestView(item))
    }
    return views
}

fun sumBatchedCoachingDigestView(view: BatchedCoachingDigestView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedFloorValue + parsedStartsAt.toDouble() + parsedLoadKg
}
