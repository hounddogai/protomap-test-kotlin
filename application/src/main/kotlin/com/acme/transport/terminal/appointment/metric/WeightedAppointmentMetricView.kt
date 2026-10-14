package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted appointment metric. */
data class WeightedAppointmentMetricView(
    var code: String,
    var name: String,
    var floorValue: String,
    var graceDays: String,
    var externalRefText: String,
    var margin: String,
    var batchSize: String,
    var region: String,
    var statusLabel: String,
)

fun toWeightedAppointmentMetricView(item: WeightedAppointmentMetric): WeightedAppointmentMetricView {
    return WeightedAppointmentMetricView(
        code = item.code,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        graceDays = item.graceDays.toString(),
        externalRefText = item.externalRef,
        margin = "%.2f".format(item.margin),
        batchSize = item.batchSize.toString(),
        region = truncate(item.region, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAppointmentMetricViews(items: MutableList<WeightedAppointmentMetric>): MutableList<WeightedAppointmentMetricView> {
    val views = mutableListOf<WeightedAppointmentMetricView>()
    for (item in items) {
        views.add(toWeightedAppointmentMetricView(item))
    }
    return views
}

fun sumWeightedAppointmentMetricView(view: WeightedAppointmentMetricView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedFloorValue + parsedGraceDays.toDouble() + parsedMargin
}
