package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict appointment variance. */
data class StrictAppointmentVarianceView(
    var code: String,
    var name: String,
    var batchSize: String,
    var score: String,
    var ceiling: String,
    var country: String,
    var budget: String,
    var rate: String,
    var unit: String,
    var region: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toStrictAppointmentVarianceView(item: StrictAppointmentVariance): StrictAppointmentVarianceView {
    return StrictAppointmentVarianceView(
        code = item.code,
        name = item.name,
        batchSize = item.batchSize.toString(),
        score = "%.2f".format(item.score),
        ceiling = "%.2f".format(item.ceiling),
        country = item.country,
        budget = "%.2f".format(item.budget),
        rate = "%.2f".format(item.rate),
        unit = item.unit,
        region = truncate(item.region, 64),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAppointmentVarianceViews(items: MutableList<StrictAppointmentVariance>): MutableList<StrictAppointmentVarianceView> {
    val views = mutableListOf<StrictAppointmentVarianceView>()
    for (item in items) {
        views.add(toStrictAppointmentVarianceView(item))
    }
    return views
}

fun sumStrictAppointmentVarianceView(view: StrictAppointmentVarianceView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedScore = view.score.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedBatchSize.toDouble() + parsedScore + parsedCeiling
}
