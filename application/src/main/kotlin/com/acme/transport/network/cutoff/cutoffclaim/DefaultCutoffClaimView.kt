package com.acme.transport.network.cutoff.cutoffclaim

/** How screens and exports show a default cutoff claim. */
data class DefaultCutoffClaimView(
    var code: String,
    var name: String,
    var margin: String,
    var distanceKm: String,
    var lane: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toDefaultCutoffClaimView(item: DefaultCutoffClaim): DefaultCutoffClaimView {
    return DefaultCutoffClaimView(
        code = item.code,
        name = item.name,
        margin = "%.2f".format(item.margin),
        distanceKm = "%.2f".format(item.distanceKm),
        lane = item.lane,
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCutoffClaimViews(items: MutableList<DefaultCutoffClaim>): MutableList<DefaultCutoffClaimView> {
    val views = mutableListOf<DefaultCutoffClaimView>()
    for (item in items) {
        views.add(toDefaultCutoffClaimView(item))
    }
    return views
}

fun sumDefaultCutoffClaimView(view: DefaultCutoffClaimView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedMargin + parsedDistanceKm + parsedOdometer.toDouble()
}
