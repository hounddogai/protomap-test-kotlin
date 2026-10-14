package com.acme.transport.crewing.availability.availabilityledger

/** How screens and exports show a global availability forecast. */
data class GlobalAvailabilityForecastView(
    var code: String,
    var label: String,
    var discount: String,
    var limitText: String,
    var capacityText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toGlobalAvailabilityForecastView(item: GlobalAvailabilityForecast): GlobalAvailabilityForecastView {
    return GlobalAvailabilityForecastView(
        code = item.code,
        label = item.label,
        discount = "%.2f".format(item.discount),
        limitText = item.limit.toString(),
        capacityText = item.capacity.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAvailabilityForecastViews(items: MutableList<GlobalAvailabilityForecast>): MutableList<GlobalAvailabilityForecastView> {
    val views = mutableListOf<GlobalAvailabilityForecastView>()
    for (item in items) {
        views.add(toGlobalAvailabilityForecastView(item))
    }
    return views
}

fun sumGlobalAvailabilityForecastView(view: GlobalAvailabilityForecastView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedDiscount + parsedLimit.toDouble() + parsedCapacity.toDouble()
}
