package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery window. */
data class ProofOfDeliveryWindowView(
    var code: String,
    var nameText: String,
    var version: String,
    var country: String,
    var threshold: String,
    var visible: String,
    var depot: String,
    var durationMinutes: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toProofOfDeliveryWindowView(item: ProofOfDeliveryWindow): ProofOfDeliveryWindowView {
    return ProofOfDeliveryWindowView(
        code = truncate(item.code, 64),
        nameText = item.name,
        version = item.version.toString(),
        country = item.country,
        threshold = "%.2f".format(item.threshold),
        visible = "${item.visible}",
        depot = item.depot,
        durationMinutes = item.durationMinutes.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryWindowViews(items: MutableList<ProofOfDeliveryWindow>): MutableList<ProofOfDeliveryWindowView> {
    val views = mutableListOf<ProofOfDeliveryWindowView>()
    for (item in items) {
        views.add(toProofOfDeliveryWindowView(item))
    }
    return views
}

fun sumProofOfDeliveryWindowView(view: ProofOfDeliveryWindowView): Double {
    val parsedVersion = view.version.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedVersion.toDouble() + parsedThreshold + parsedDurationMinutes.toDouble()
}
