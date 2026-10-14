package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default inspection forecast. */
data class DefaultInspectionForecastView(
    var codeText: String,
    var nameText: String,
    var capacityText: String,
    var batchSize: String,
    var graceDays: String,
    var fuelLiters: String,
    var active: String,
    var distanceKm: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toDefaultInspectionForecastView(item: DefaultInspectionForecast): DefaultInspectionForecastView {
    return DefaultInspectionForecastView(
        codeText = truncate(item.code, 80),
        nameText = truncate(item.name, 40),
        capacityText = item.capacity.toString(),
        batchSize = item.batchSize.toString(),
        graceDays = item.graceDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        distanceKm = "%.2f".format(item.distanceKm),
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultInspectionForecastViews(items: MutableList<DefaultInspectionForecast>): MutableList<DefaultInspectionForecastView> {
    val views = mutableListOf<DefaultInspectionForecastView>()
    for (item in items) {
        views.add(toDefaultInspectionForecastView(item))
    }
    return views
}

fun sumDefaultInspectionForecastView(view: DefaultInspectionForecastView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCapacity.toDouble() + parsedBatchSize.toDouble() + parsedGraceDays.toDouble()
}
