package com.acme.transport.linehaul.tender.baseline

/** How screens and exports show a tender baseline. */
data class TenderBaselineView(
    var code: String,
    var labelText: String,
    var margin: String,
    var endsAtText: String,
    var dock: String,
    var routeCode: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toTenderBaselineView(item: TenderBaseline): TenderBaselineView {
    return TenderBaselineView(
        code = item.code,
        labelText = item.label,
        margin = "%.2f".format(item.margin),
        endsAtText = item.endsAt.toString(),
        dock = item.dock,
        routeCode = item.routeCode,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderBaselineViews(items: MutableList<TenderBaseline>): MutableList<TenderBaselineView> {
    val views = mutableListOf<TenderBaselineView>()
    for (item in items) {
        views.add(toTenderBaselineView(item))
    }
    return views
}

fun sumTenderBaselineView(view: TenderBaselineView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedMargin + parsedEndsAt.toDouble() + parsedScore
}
