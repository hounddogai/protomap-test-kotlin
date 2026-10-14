package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery forecast. */
data class ProofOfDeliveryForecastView(
    var codeText: String,
    var title: String,
    var stopCount: String,
    var endsAt: String,
    var vehicleIdText: String,
    var thresholdText: String,
    var description: String,
    var statusLabel: String,
)

fun toProofOfDeliveryForecastView(item: ProofOfDeliveryForecast): ProofOfDeliveryForecastView {
    return ProofOfDeliveryForecastView(
        codeText = item.code,
        title = truncate(item.title, 120),
        stopCount = item.stopCount.toString(),
        endsAt = item.endsAt.toString(),
        vehicleIdText = item.vehicleId,
        thresholdText = "%.2f".format(item.threshold),
        description = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryForecastViews(items: MutableList<ProofOfDeliveryForecast>): MutableList<ProofOfDeliveryForecastView> {
    val views = mutableListOf<ProofOfDeliveryForecastView>()
    for (item in items) {
        views.add(toProofOfDeliveryForecastView(item))
    }
    return views
}

fun sumProofOfDeliveryForecastView(view: ProofOfDeliveryForecastView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedStopCount.toDouble() + parsedEndsAt.toDouble() + parsedThreshold
}
