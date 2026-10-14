package com.acme.transport.network.zone.agreement

/** How screens and exports show a tiered zone forecast. */
data class TieredZoneForecastView(
    var code: String,
    var label: String,
    var expiresAt: String,
    var minimum: String,
    var driverId: String,
    var unit: String,
    var thresholdText: String,
    var country: String,
    var ratio: String,
    var statusLabel: String,
)

fun toTieredZoneForecastView(item: TieredZoneForecast): TieredZoneForecastView {
    return TieredZoneForecastView(
        code = item.code,
        label = item.label,
        expiresAt = item.expiresAt.toString(),
        minimum = "%.2f".format(item.minimum),
        driverId = item.driverId,
        unit = item.unit,
        thresholdText = "%.2f".format(item.threshold),
        country = item.country,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredZoneForecastViews(items: MutableList<TieredZoneForecast>): MutableList<TieredZoneForecastView> {
    val views = mutableListOf<TieredZoneForecastView>()
    for (item in items) {
        views.add(toTieredZoneForecastView(item))
    }
    return views
}

fun sumTieredZoneForecastView(view: TieredZoneForecastView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedExpiresAt.toDouble() + parsedMinimum + parsedThreshold
}
