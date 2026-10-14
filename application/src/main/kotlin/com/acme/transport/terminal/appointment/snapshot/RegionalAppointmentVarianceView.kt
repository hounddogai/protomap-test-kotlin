package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional appointment variance. */
data class RegionalAppointmentVarianceView(
    var code: String,
    var name: String,
    var visible: String,
    var loadKg: String,
    var cost: String,
    var unit: String,
    var carrierText: String,
    var locale: String,
    var startsAt: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toRegionalAppointmentVarianceView(item: RegionalAppointmentVariance): RegionalAppointmentVarianceView {
    return RegionalAppointmentVarianceView(
        code = item.code,
        name = item.name,
        visible = "${item.visible}",
        loadKg = "%.2f".format(item.loadKg),
        cost = "%.2f".format(item.cost),
        unit = truncate(item.unit, 64),
        carrierText = item.carrier,
        locale = truncate(item.locale, 120),
        startsAt = item.startsAt.toString(),
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAppointmentVarianceViews(items: MutableList<RegionalAppointmentVariance>): MutableList<RegionalAppointmentVarianceView> {
    val views = mutableListOf<RegionalAppointmentVarianceView>()
    for (item in items) {
        views.add(toRegionalAppointmentVarianceView(item))
    }
    return views
}

fun sumRegionalAppointmentVarianceView(view: RegionalAppointmentVarianceView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedLoadKg + parsedCost + parsedStartsAt.toDouble()
}
