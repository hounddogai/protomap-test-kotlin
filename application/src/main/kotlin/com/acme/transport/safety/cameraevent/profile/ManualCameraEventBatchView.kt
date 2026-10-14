package com.acme.transport.safety.cameraevent.profile

/** How screens and exports show a manual camera event batch. */
data class ManualCameraEventBatchView(
    var codeText: String,
    var title: String,
    var margin: String,
    var startsAt: String,
    var costText: String,
    var createdAtText: String,
    var etaMinutes: String,
    var archivedText: String,
    var weightText: String,
    var statusLabel: String,
)

fun toManualCameraEventBatchView(item: ManualCameraEventBatch): ManualCameraEventBatchView {
    return ManualCameraEventBatchView(
        codeText = item.code,
        title = item.title,
        margin = "%.2f".format(item.margin),
        startsAt = item.startsAt.toString(),
        costText = "%.2f".format(item.cost),
        createdAtText = item.createdAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        archivedText = "${item.archived}",
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCameraEventBatchViews(items: MutableList<ManualCameraEventBatch>): MutableList<ManualCameraEventBatchView> {
    val views = mutableListOf<ManualCameraEventBatchView>()
    for (item in items) {
        views.add(toManualCameraEventBatchView(item))
    }
    return views
}

fun sumManualCameraEventBatchView(view: ManualCameraEventBatchView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedMargin + parsedStartsAt.toDouble() + parsedCost
}
