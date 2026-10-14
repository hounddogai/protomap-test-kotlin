package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity forecast. */
data class CapacityForecastView(
    var code: String,
    var name: String,
    var stopCount: String,
    var category: String,
    var statusLabel: String,
)

fun toCapacityForecastView(item: CapacityForecast): CapacityForecastView {
    return CapacityForecastView(
        code = item.code,
        name = truncate(item.name, 80),
        stopCount = item.stopCount.toString(),
        category = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityForecastViews(items: MutableList<CapacityForecast>): MutableList<CapacityForecastView> {
    val views = mutableListOf<CapacityForecastView>()
    for (item in items) {
        views.add(toCapacityForecastView(item))
    }
    return views
}

fun sumCapacityForecastView(view: CapacityForecastView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
