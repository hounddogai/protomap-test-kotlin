package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dispute claim. */
data class SeasonalDisputeClaimView(
    var code: String,
    var name: String,
    var carrier: String,
    var timezone: String,
    var windowDays: String,
    var distanceKm: String,
    var locale: String,
    var statusLabel: String,
)

fun toSeasonalDisputeClaimView(item: SeasonalDisputeClaim): SeasonalDisputeClaimView {
    return SeasonalDisputeClaimView(
        code = truncate(item.code, 64),
        name = item.name,
        carrier = item.carrier,
        timezone = item.timezone,
        windowDays = item.windowDays.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        locale = truncate(item.locale, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDisputeClaimViews(items: MutableList<SeasonalDisputeClaim>): MutableList<SeasonalDisputeClaimView> {
    val views = mutableListOf<SeasonalDisputeClaimView>()
    for (item in items) {
        views.add(toSeasonalDisputeClaimView(item))
    }
    return views
}

fun sumSeasonalDisputeClaimView(view: SeasonalDisputeClaimView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedWindowDays.toDouble() + parsedDistanceKm
}
