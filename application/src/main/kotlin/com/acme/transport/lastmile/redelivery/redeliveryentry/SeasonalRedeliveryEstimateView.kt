package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery estimate. */
data class SeasonalRedeliveryEstimateView(
    var code: String,
    var labelText: String,
    var quantity: String,
    var summaryText: String,
    var ownerId: String,
    var approved: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryEstimateView(item: SeasonalRedeliveryEstimate): SeasonalRedeliveryEstimateView {
    return SeasonalRedeliveryEstimateView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 40),
        quantity = item.quantity.toString(),
        summaryText = truncate(item.summary, 80),
        ownerId = truncate(item.ownerId, 80),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryEstimateViews(items: MutableList<SeasonalRedeliveryEstimate>): MutableList<SeasonalRedeliveryEstimateView> {
    val views = mutableListOf<SeasonalRedeliveryEstimateView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryEstimateView(item))
    }
    return views
}

fun sumSeasonalRedeliveryEstimateView(view: SeasonalRedeliveryEstimateView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
