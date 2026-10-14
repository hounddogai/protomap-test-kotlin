package com.acme.transport.freightpay.ratecard.variance

/** How screens and exports show a fallback rate card variance. */
data class FallbackRateCardVarianceView(
    var code: String,
    var title: String,
    var distanceKmText: String,
    var graceDaysText: String,
    var startsAtText: String,
    var retries: String,
    var statusLabel: String,
)

fun toFallbackRateCardVarianceView(item: FallbackRateCardVariance): FallbackRateCardVarianceView {
    return FallbackRateCardVarianceView(
        code = item.code,
        title = item.title,
        distanceKmText = "%.2f".format(item.distanceKm),
        graceDaysText = item.graceDays.toString(),
        startsAtText = item.startsAt.toString(),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRateCardVarianceViews(items: MutableList<FallbackRateCardVariance>): MutableList<FallbackRateCardVarianceView> {
    val views = mutableListOf<FallbackRateCardVarianceView>()
    for (item in items) {
        views.add(toFallbackRateCardVarianceView(item))
    }
    return views
}

fun sumFallbackRateCardVarianceView(view: FallbackRateCardVarianceView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedDistanceKm + parsedGraceDays.toDouble() + parsedStartsAt.toDouble()
}
