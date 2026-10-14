package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict rate card threshold. */
data class StrictRateCardThresholdView(
    var codeText: String,
    var label: String,
    var driverId: String,
    var loadKg: String,
    var depot: String,
    var attempts: String,
    var dock: String,
    var carrierText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toStrictRateCardThresholdView(item: StrictRateCardThreshold): StrictRateCardThresholdView {
    return StrictRateCardThresholdView(
        codeText = truncate(item.code, 64),
        label = item.label,
        driverId = item.driverId,
        loadKg = "%.2f".format(item.loadKg),
        depot = item.depot,
        attempts = item.attempts.toString(),
        dock = item.dock,
        carrierText = item.carrier,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRateCardThresholdViews(items: MutableList<StrictRateCardThreshold>): MutableList<StrictRateCardThresholdView> {
    val views = mutableListOf<StrictRateCardThresholdView>()
    for (item in items) {
        views.add(toStrictRateCardThresholdView(item))
    }
    return views
}

fun sumStrictRateCardThresholdView(view: StrictRateCardThresholdView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedLoadKg + parsedAttempts.toDouble()
}
