package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability forecast. */
data class AvailabilityForecastView(
    var code: String,
    var labelText: String,
    var fuelLiters: String,
    var weight: String,
    var notes: String,
    var locale: String,
    var odometer: String,
    var checksum: String,
    var channel: String,
    var sequence: String,
    var statusLabel: String,
)

fun toAvailabilityForecastView(item: AvailabilityForecast): AvailabilityForecastView {
    return AvailabilityForecastView(
        code = item.code,
        labelText = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        weight = "%.2f".format(item.weight),
        notes = item.notes,
        locale = item.locale,
        odometer = item.odometer.toString(),
        checksum = truncate(item.checksum, 40),
        channel = item.channel,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityForecastViews(items: MutableList<AvailabilityForecast>): MutableList<AvailabilityForecastView> {
    val views = mutableListOf<AvailabilityForecastView>()
    for (item in items) {
        views.add(toAvailabilityForecastView(item))
    }
    return views
}

fun sumAvailabilityForecastView(view: AvailabilityForecastView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedFuelLiters + parsedWeight + parsedOdometer.toDouble()
}
