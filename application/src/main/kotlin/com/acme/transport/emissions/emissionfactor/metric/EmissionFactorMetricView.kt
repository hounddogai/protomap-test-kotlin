package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor metric. */
data class EmissionFactorMetricView(
    var code: String,
    var labelText: String,
    var endsAtText: String,
    var phone: String,
    var ownerId: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toEmissionFactorMetricView(item: EmissionFactorMetric): EmissionFactorMetricView {
    return EmissionFactorMetricView(
        code = item.code,
        labelText = item.label,
        endsAtText = item.endsAt.toString(),
        phone = item.phone,
        ownerId = truncate(item.ownerId, 40),
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorMetricViews(items: MutableList<EmissionFactorMetric>): MutableList<EmissionFactorMetricView> {
    val views = mutableListOf<EmissionFactorMetricView>()
    for (item in items) {
        views.add(toEmissionFactorMetricView(item))
    }
    return views
}

fun sumEmissionFactorMetricView(view: EmissionFactorMetricView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEndsAt.toDouble()
}
