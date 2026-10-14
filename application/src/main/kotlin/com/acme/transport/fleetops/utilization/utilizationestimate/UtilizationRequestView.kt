package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization request. */
data class UtilizationRequestView(
    var codeText: String,
    var label: String,
    var carrier: String,
    var contactEmail: String,
    var region: String,
    var channel: String,
    var driverIdText: String,
    var attempts: String,
    var etaMinutes: String,
    var minimum: String,
    var statusLabel: String,
)

fun toUtilizationRequestView(item: UtilizationRequest): UtilizationRequestView {
    return UtilizationRequestView(
        codeText = truncate(item.code, 40),
        label = item.label,
        carrier = item.carrier,
        contactEmail = item.contactEmail,
        region = truncate(item.region, 120),
        channel = item.channel,
        driverIdText = item.driverId,
        attempts = item.attempts.toString(),
        etaMinutes = item.etaMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationRequestViews(items: MutableList<UtilizationRequest>): MutableList<UtilizationRequestView> {
    val views = mutableListOf<UtilizationRequestView>()
    for (item in items) {
        views.add(toUtilizationRequestView(item))
    }
    return views
}

fun sumUtilizationRequestView(view: UtilizationRequestView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedAttempts.toDouble() + parsedEtaMinutes.toDouble() + parsedMinimum
}
