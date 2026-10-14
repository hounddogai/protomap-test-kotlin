package com.acme.transport.customs.clearance.segment

/** How screens and exports show a clearance segment. */
data class ClearanceSegmentView(
    var code: String,
    var titleText: String,
    var graceDays: String,
    var loadKg: String,
    var margin: String,
    var ceiling: String,
    var pallets: String,
    var statusLabel: String,
)

fun toClearanceSegmentView(item: ClearanceSegment): ClearanceSegmentView {
    return ClearanceSegmentView(
        code = item.code,
        titleText = item.title,
        graceDays = item.graceDays.toString(),
        loadKg = "%.2f".format(item.loadKg),
        margin = "%.2f".format(item.margin),
        ceiling = "%.2f".format(item.ceiling),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceSegmentViews(items: MutableList<ClearanceSegment>): MutableList<ClearanceSegmentView> {
    val views = mutableListOf<ClearanceSegmentView>()
    for (item in items) {
        views.add(toClearanceSegmentView(item))
    }
    return views
}

fun sumClearanceSegmentView(view: ClearanceSegmentView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedGraceDays.toDouble() + parsedLoadKg + parsedMargin
}
