package com.acme.transport.network.servicearea.exception

/** How screens and exports show a regional service area threshold. */
data class RegionalServiceAreaThresholdView(
    var codeText: String,
    var nameText: String,
    var score: String,
    var margin: String,
    var discount: String,
    var category: String,
    var statusLabel: String,
)

fun toRegionalServiceAreaThresholdView(item: RegionalServiceAreaThreshold): RegionalServiceAreaThresholdView {
    return RegionalServiceAreaThresholdView(
        codeText = item.code,
        nameText = item.name,
        score = "%.2f".format(item.score),
        margin = "%.2f".format(item.margin),
        discount = "%.2f".format(item.discount),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalServiceAreaThresholdViews(items: MutableList<RegionalServiceAreaThreshold>): MutableList<RegionalServiceAreaThresholdView> {
    val views = mutableListOf<RegionalServiceAreaThresholdView>()
    for (item in items) {
        views.add(toRegionalServiceAreaThresholdView(item))
    }
    return views
}

fun sumRegionalServiceAreaThresholdView(view: RegionalServiceAreaThresholdView): Double {
    val parsedScore = view.score.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedScore + parsedMargin + parsedDiscount
}
