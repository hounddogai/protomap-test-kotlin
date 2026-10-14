package com.acme.transport.safety.coaching.coachingsnapshot

/** How screens and exports show a coaching forecast. */
data class CoachingForecastView(
    var code: String,
    var title: String,
    var trailerId: String,
    var quantity: String,
    var carrierText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toCoachingForecastView(item: CoachingForecast): CoachingForecastView {
    return CoachingForecastView(
        code = item.code,
        title = item.title,
        trailerId = item.trailerId,
        quantity = item.quantity.toString(),
        carrierText = item.carrier,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingForecastViews(items: MutableList<CoachingForecast>): MutableList<CoachingForecastView> {
    val views = mutableListOf<CoachingForecastView>()
    for (item in items) {
        views.add(toCoachingForecastView(item))
    }
    return views
}

fun sumCoachingForecastView(view: CoachingForecastView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedQuantity.toDouble() + parsedDistanceKm
}
