package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial template. */
data class AccessorialTemplateView(
    var codeText: String,
    var name: String,
    var externalRef: String,
    var channel: String,
    var expiresAt: String,
    var odometerText: String,
    var carrier: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toAccessorialTemplateView(item: AccessorialTemplate): AccessorialTemplateView {
    return AccessorialTemplateView(
        codeText = item.code,
        name = truncate(item.name, 40),
        externalRef = truncate(item.externalRef, 64),
        channel = item.channel,
        expiresAt = item.expiresAt.toString(),
        odometerText = item.odometer.toString(),
        carrier = item.carrier,
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialTemplateViews(items: MutableList<AccessorialTemplate>): MutableList<AccessorialTemplateView> {
    val views = mutableListOf<AccessorialTemplateView>()
    for (item in items) {
        views.add(toAccessorialTemplateView(item))
    }
    return views
}

fun sumAccessorialTemplateView(view: AccessorialTemplateView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedExpiresAt.toDouble() + parsedOdometer.toDouble()
}
