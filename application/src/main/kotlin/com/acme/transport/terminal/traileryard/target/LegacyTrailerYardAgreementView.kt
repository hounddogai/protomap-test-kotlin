package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy trailer yard agreement. */
data class LegacyTrailerYardAgreementView(
    var code: String,
    var title: String,
    var version: String,
    var currency: String,
    var odometer: String,
    var depot: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toLegacyTrailerYardAgreementView(item: LegacyTrailerYardAgreement): LegacyTrailerYardAgreementView {
    return LegacyTrailerYardAgreementView(
        code = item.code,
        title = item.title,
        version = item.version.toString(),
        currency = truncate(item.currency, 80),
        odometer = item.odometer.toString(),
        depot = item.depot,
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerYardAgreementViews(items: MutableList<LegacyTrailerYardAgreement>): MutableList<LegacyTrailerYardAgreementView> {
    val views = mutableListOf<LegacyTrailerYardAgreementView>()
    for (item in items) {
        views.add(toLegacyTrailerYardAgreementView(item))
    }
    return views
}

fun sumLegacyTrailerYardAgreementView(view: LegacyTrailerYardAgreementView): Double {
    val parsedVersion = view.version.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedVersion.toDouble() + parsedOdometer.toDouble()
}
