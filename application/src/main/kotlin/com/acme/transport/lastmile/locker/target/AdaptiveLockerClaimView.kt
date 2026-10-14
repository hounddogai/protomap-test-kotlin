package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive locker claim. */
data class AdaptiveLockerClaimView(
    var code: String,
    var labelText: String,
    var depot: String,
    var rate: String,
    var lane: String,
    var regionText: String,
    var ratio: String,
    var etaMinutes: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toAdaptiveLockerClaimView(item: AdaptiveLockerClaim): AdaptiveLockerClaimView {
    return AdaptiveLockerClaimView(
        code = item.code,
        labelText = item.label,
        depot = item.depot,
        rate = "%.2f".format(item.rate),
        lane = item.lane,
        regionText = truncate(item.region, 80),
        ratio = "%.2f".format(item.ratio),
        etaMinutes = item.etaMinutes.toString(),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLockerClaimViews(items: MutableList<AdaptiveLockerClaim>): MutableList<AdaptiveLockerClaimView> {
    val views = mutableListOf<AdaptiveLockerClaimView>()
    for (item in items) {
        views.add(toAdaptiveLockerClaimView(item))
    }
    return views
}

fun sumAdaptiveLockerClaimView(view: AdaptiveLockerClaimView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedRate + parsedRatio + parsedEtaMinutes.toDouble()
}
