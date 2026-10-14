package com.acme.transport.network.cutoff.batch

/** How screens and exports show a primary cutoff score. */
data class PrimaryCutoffScoreView(
    var code: String,
    var labelText: String,
    var visibleText: String,
    var minimumText: String,
    var unit: String,
    var contactEmail: String,
    var segmentText: String,
    var depot: String,
    var margin: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toPrimaryCutoffScoreView(item: PrimaryCutoffScore): PrimaryCutoffScoreView {
    return PrimaryCutoffScoreView(
        code = item.code,
        labelText = item.label,
        visibleText = "${item.visible}",
        minimumText = "%.2f".format(item.minimum),
        unit = item.unit,
        contactEmail = item.contactEmail,
        segmentText = item.segment,
        depot = item.depot,
        margin = "%.2f".format(item.margin),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCutoffScoreViews(items: MutableList<PrimaryCutoffScore>): MutableList<PrimaryCutoffScoreView> {
    val views = mutableListOf<PrimaryCutoffScoreView>()
    for (item in items) {
        views.add(toPrimaryCutoffScoreView(item))
    }
    return views
}

fun sumPrimaryCutoffScoreView(view: PrimaryCutoffScoreView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedMinimum + parsedMargin
}
