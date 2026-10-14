package com.acme.transport.crewing.restbreak.overrideitem

/** How screens and exports show a legacy rest break estimate. */
data class LegacyRestBreakEstimateView(
    var code: String,
    var title: String,
    var label: String,
    var createdAtText: String,
    var reviewerText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toLegacyRestBreakEstimateView(item: LegacyRestBreakEstimate): LegacyRestBreakEstimateView {
    return LegacyRestBreakEstimateView(
        code = item.code,
        title = item.title,
        label = item.label,
        createdAtText = item.createdAt.toString(),
        reviewerText = item.reviewer,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRestBreakEstimateViews(items: MutableList<LegacyRestBreakEstimate>): MutableList<LegacyRestBreakEstimateView> {
    val views = mutableListOf<LegacyRestBreakEstimateView>()
    for (item in items) {
        views.add(toLegacyRestBreakEstimateView(item))
    }
    return views
}

fun sumLegacyRestBreakEstimateView(view: LegacyRestBreakEstimateView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCreatedAt.toDouble() + parsedLoadKg
}
