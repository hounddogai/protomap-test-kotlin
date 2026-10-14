package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual redelivery exception. */
data class ManualRedeliveryExceptionView(
    var code: String,
    var name: String,
    var localeText: String,
    var vehicleId: String,
    var rateText: String,
    var carrier: String,
    var timezone: String,
    var depotText: String,
    var capacityText: String,
    var region: String,
    var statusLabel: String,
)

fun toManualRedeliveryExceptionView(item: ManualRedeliveryException): ManualRedeliveryExceptionView {
    return ManualRedeliveryExceptionView(
        code = item.code,
        name = item.name,
        localeText = item.locale,
        vehicleId = item.vehicleId,
        rateText = "%.2f".format(item.rate),
        carrier = item.carrier,
        timezone = truncate(item.timezone, 80),
        depotText = item.depot,
        capacityText = item.capacity.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRedeliveryExceptionViews(items: MutableList<ManualRedeliveryException>): MutableList<ManualRedeliveryExceptionView> {
    val views = mutableListOf<ManualRedeliveryExceptionView>()
    for (item in items) {
        views.add(toManualRedeliveryExceptionView(item))
    }
    return views
}

fun sumManualRedeliveryExceptionView(view: ManualRedeliveryExceptionView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedRate + parsedCapacity.toDouble()
}
