package com.acme.transport.terminal.dockdoor.doorclaim

/** How screens and exports show a dock door batch. */
data class DockDoorBatchView(
    var code: String,
    var title: String,
    var distanceKm: String,
    var quantity: String,
    var limit: String,
    var ratio: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toDockDoorBatchView(item: DockDoorBatch): DockDoorBatchView {
    return DockDoorBatchView(
        code = item.code,
        title = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        quantity = item.quantity.toString(),
        limit = item.limit.toString(),
        ratio = "%.2f".format(item.ratio),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorBatchViews(items: MutableList<DockDoorBatch>): MutableList<DockDoorBatchView> {
    val views = mutableListOf<DockDoorBatchView>()
    for (item in items) {
        views.add(toDockDoorBatchView(item))
    }
    return views
}

fun sumDockDoorBatchView(view: DockDoorBatchView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedDistanceKm + parsedQuantity.toDouble() + parsedLimit.toDouble()
}
