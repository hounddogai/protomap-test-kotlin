package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect metric. */
data class DefectMetricView(
    var code: String,
    var name: String,
    var countryText: String,
    var unit: String,
    var discount: String,
    var channel: String,
    var carrierText: String,
    var createdAtText: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toDefectMetricView(item: DefectMetric): DefectMetricView {
    return DefectMetricView(
        code = item.code,
        name = item.name,
        countryText = item.country,
        unit = truncate(item.unit, 120),
        discount = "%.2f".format(item.discount),
        channel = truncate(item.channel, 80),
        carrierText = truncate(item.carrier, 40),
        createdAtText = item.createdAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectMetricViews(items: MutableList<DefectMetric>): MutableList<DefectMetricView> {
    val views = mutableListOf<DefectMetricView>()
    for (item in items) {
        views.add(toDefectMetricView(item))
    }
    return views
}

fun sumDefectMetricView(view: DefectMetricView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDiscount + parsedCreatedAt.toDouble() + parsedDurationMinutes.toDouble()
}
