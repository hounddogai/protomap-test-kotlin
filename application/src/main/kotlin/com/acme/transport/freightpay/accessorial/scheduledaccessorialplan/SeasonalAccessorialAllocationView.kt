package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

/** How screens and exports show a seasonal accessorial allocation. */
data class SeasonalAccessorialAllocationView(
    var code: String,
    var label: String,
    var ceiling: String,
    var summaryText: String,
    var amount: String,
    var margin: String,
    var endsAtText: String,
    var visibleText: String,
    var carrier: String,
    var trailerIdText: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toSeasonalAccessorialAllocationView(item: SeasonalAccessorialAllocation): SeasonalAccessorialAllocationView {
    return SeasonalAccessorialAllocationView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        summaryText = item.summary,
        amount = "%.2f".format(item.amount),
        margin = "%.2f".format(item.margin),
        endsAtText = item.endsAt.toString(),
        visibleText = "${item.visible}",
        carrier = item.carrier,
        trailerIdText = item.trailerId,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalAccessorialAllocationViews(items: MutableList<SeasonalAccessorialAllocation>): MutableList<SeasonalAccessorialAllocationView> {
    val views = mutableListOf<SeasonalAccessorialAllocationView>()
    for (item in items) {
        views.add(toSeasonalAccessorialAllocationView(item))
    }
    return views
}

fun sumSeasonalAccessorialAllocationView(view: SeasonalAccessorialAllocationView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedCeiling + parsedAmount + parsedMargin
}
