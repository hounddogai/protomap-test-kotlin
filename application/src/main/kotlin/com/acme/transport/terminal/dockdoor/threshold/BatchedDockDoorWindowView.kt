package com.acme.transport.terminal.dockdoor.threshold

/** How screens and exports show a batched dock door window. */
data class BatchedDockDoorWindowView(
    var code: String,
    var name: String,
    var rate: String,
    var approvedText: String,
    var trailerId: String,
    var active: String,
    var priority: String,
    var versionText: String,
    var capacity: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toBatchedDockDoorWindowView(item: BatchedDockDoorWindow): BatchedDockDoorWindowView {
    return BatchedDockDoorWindowView(
        code = item.code,
        name = item.name,
        rate = "%.2f".format(item.rate),
        approvedText = "${item.approved}",
        trailerId = item.trailerId,
        active = "${item.active}",
        priority = item.priority.toString(),
        versionText = item.version.toString(),
        capacity = item.capacity.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDockDoorWindowViews(items: MutableList<BatchedDockDoorWindow>): MutableList<BatchedDockDoorWindowView> {
    val views = mutableListOf<BatchedDockDoorWindowView>()
    for (item in items) {
        views.add(toBatchedDockDoorWindowView(item))
    }
    return views
}

fun sumBatchedDockDoorWindowView(view: BatchedDockDoorWindowView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedRate + parsedPriority.toDouble() + parsedVersion.toDouble()
}
