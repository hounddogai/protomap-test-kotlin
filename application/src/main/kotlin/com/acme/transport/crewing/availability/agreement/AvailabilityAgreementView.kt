package com.acme.transport.crewing.availability.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability agreement. */
data class AvailabilityAgreementView(
    var code: String,
    var label: String,
    var laneText: String,
    var odometer: String,
    var ratioText: String,
    var dockText: String,
    var attempts: String,
    var region: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toAvailabilityAgreementView(item: AvailabilityAgreement): AvailabilityAgreementView {
    return AvailabilityAgreementView(
        code = item.code,
        label = item.label,
        laneText = truncate(item.lane, 80),
        odometer = item.odometer.toString(),
        ratioText = "%.2f".format(item.ratio),
        dockText = truncate(item.dock, 80),
        attempts = item.attempts.toString(),
        region = item.region,
        routeCode = truncate(item.routeCode, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityAgreementViews(items: MutableList<AvailabilityAgreement>): MutableList<AvailabilityAgreementView> {
    val views = mutableListOf<AvailabilityAgreementView>()
    for (item in items) {
        views.add(toAvailabilityAgreementView(item))
    }
    return views
}

fun sumAvailabilityAgreementView(view: AvailabilityAgreementView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedOdometer.toDouble() + parsedRatio + parsedAttempts.toDouble()
}
