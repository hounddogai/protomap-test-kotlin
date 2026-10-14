package com.acme.transport.network.servicearea.areaassignment

/** How screens and exports show a capped service area assignment. */
data class CappedServiceAreaAssignmentView(
    var code: String,
    var nameText: String,
    var stopCountText: String,
    var limit: String,
    var startsAt: String,
    var sequence: String,
    var statusLabel: String,
)

fun toCappedServiceAreaAssignmentView(item: CappedServiceAreaAssignment): CappedServiceAreaAssignmentView {
    return CappedServiceAreaAssignmentView(
        code = item.code,
        nameText = item.name,
        stopCountText = item.stopCount.toString(),
        limit = item.limit.toString(),
        startsAt = item.startsAt.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedServiceAreaAssignmentViews(items: MutableList<CappedServiceAreaAssignment>): MutableList<CappedServiceAreaAssignmentView> {
    val views = mutableListOf<CappedServiceAreaAssignmentView>()
    for (item in items) {
        views.add(toCappedServiceAreaAssignmentView(item))
    }
    return views
}

fun sumCappedServiceAreaAssignmentView(view: CappedServiceAreaAssignmentView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStopCount.toDouble() + parsedLimit.toDouble() + parsedStartsAt.toDouble()
}
