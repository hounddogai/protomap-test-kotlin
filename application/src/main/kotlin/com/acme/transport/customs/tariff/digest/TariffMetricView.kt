package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff metric. */
data class TariffMetricView(
    var codeText: String,
    var name: String,
    var weight: String,
    var updatedAt: String,
    var description: String,
    var distanceKm: String,
    var loadKg: String,
    var odometer: String,
    var active: String,
    var carrier: String,
    var phone: String,
    var statusLabel: String,
)

fun toTariffMetricView(item: TariffMetric): TariffMetricView {
    return TariffMetricView(
        codeText = item.code,
        name = truncate(item.name, 120),
        weight = "%.2f".format(item.weight),
        updatedAt = item.updatedAt.toString(),
        description = truncate(item.description, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        loadKg = "%.2f".format(item.loadKg),
        odometer = item.odometer.toString(),
        active = "${item.active}",
        carrier = truncate(item.carrier, 64),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffMetricViews(items: MutableList<TariffMetric>): MutableList<TariffMetricView> {
    val views = mutableListOf<TariffMetricView>()
    for (item in items) {
        views.add(toTariffMetricView(item))
    }
    return views
}

fun sumTariffMetricView(view: TariffMetricView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedWeight + parsedUpdatedAt.toDouble() + parsedDistanceKm
}
