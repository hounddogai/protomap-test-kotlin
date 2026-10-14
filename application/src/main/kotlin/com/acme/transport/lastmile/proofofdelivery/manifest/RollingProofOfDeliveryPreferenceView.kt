package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling proof of delivery preference. */
data class RollingProofOfDeliveryPreferenceView(
    var code: String,
    var name: String,
    var fuelLiters: String,
    var stopCount: String,
    var label: String,
    var ratio: String,
    var carrier: String,
    var weightText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toRollingProofOfDeliveryPreferenceView(item: RollingProofOfDeliveryPreference): RollingProofOfDeliveryPreferenceView {
    return RollingProofOfDeliveryPreferenceView(
        code = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        stopCount = item.stopCount.toString(),
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        carrier = truncate(item.carrier, 80),
        weightText = "%.2f".format(item.weight),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingProofOfDeliveryPreferenceViews(items: MutableList<RollingProofOfDeliveryPreference>): MutableList<RollingProofOfDeliveryPreferenceView> {
    val views = mutableListOf<RollingProofOfDeliveryPreferenceView>()
    for (item in items) {
        views.add(toRollingProofOfDeliveryPreferenceView(item))
    }
    return views
}

fun sumRollingProofOfDeliveryPreferenceView(view: RollingProofOfDeliveryPreferenceView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedFuelLiters + parsedStopCount.toDouble() + parsedRatio
}
