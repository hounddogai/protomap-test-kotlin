package com.acme.transport.crewing.restbreak.breaksummary

/** How screens and exports show a seasonal rest break estimate. */
data class SeasonalRestBreakEstimateView(
    var codeText: String,
    var name: String,
    var margin: String,
    var minimumText: String,
    var pinned: String,
    var source: String,
    var loadKg: String,
    var summaryText: String,
    var capacity: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toSeasonalRestBreakEstimateView(item: SeasonalRestBreakEstimate): SeasonalRestBreakEstimateView {
    return SeasonalRestBreakEstimateView(
        codeText = item.code,
        name = item.name,
        margin = "%.2f".format(item.margin),
        minimumText = "%.2f".format(item.minimum),
        pinned = "${item.pinned}",
        source = item.source,
        loadKg = "%.2f".format(item.loadKg),
        summaryText = item.summary,
        capacity = item.capacity.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRestBreakEstimateViews(items: MutableList<SeasonalRestBreakEstimate>): MutableList<SeasonalRestBreakEstimateView> {
    val views = mutableListOf<SeasonalRestBreakEstimateView>()
    for (item in items) {
        views.add(toSeasonalRestBreakEstimateView(item))
    }
    return views
}

fun sumSeasonalRestBreakEstimateView(view: SeasonalRestBreakEstimateView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedMargin + parsedMinimum + parsedLoadKg
}
