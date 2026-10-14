package com.acme.transport.customs.hold.checkpoint

/** How screens and exports show a capped hold checkpoint. */
data class CappedHoldCheckpointView(
    var codeText: String,
    var title: String,
    var durationMinutes: String,
    var margin: String,
    var costText: String,
    var approvedText: String,
    var phone: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toCappedHoldCheckpointView(item: CappedHoldCheckpoint): CappedHoldCheckpointView {
    return CappedHoldCheckpointView(
        codeText = item.code,
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        margin = "%.2f".format(item.margin),
        costText = "%.2f".format(item.cost),
        approvedText = "${item.approved}",
        phone = item.phone,
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHoldCheckpointViews(items: MutableList<CappedHoldCheckpoint>): MutableList<CappedHoldCheckpointView> {
    val views = mutableListOf<CappedHoldCheckpointView>()
    for (item in items) {
        views.add(toCappedHoldCheckpointView(item))
    }
    return views
}

fun sumCappedHoldCheckpointView(view: CappedHoldCheckpointView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedMargin + parsedCost
}
