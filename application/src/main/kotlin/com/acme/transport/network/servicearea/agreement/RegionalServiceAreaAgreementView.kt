package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional service area agreement. */
data class RegionalServiceAreaAgreementView(
    var code: String,
    var name: String,
    var odometer: String,
    var driverId: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toRegionalServiceAreaAgreementView(item: RegionalServiceAreaAgreement): RegionalServiceAreaAgreementView {
    return RegionalServiceAreaAgreementView(
        code = item.code,
        name = item.name,
        odometer = item.odometer.toString(),
        driverId = truncate(item.driverId, 120),
        shiftCode = truncate(item.shiftCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalServiceAreaAgreementViews(items: MutableList<RegionalServiceAreaAgreement>): MutableList<RegionalServiceAreaAgreementView> {
    val views = mutableListOf<RegionalServiceAreaAgreementView>()
    for (item in items) {
        views.add(toRegionalServiceAreaAgreementView(item))
    }
    return views
}

fun sumRegionalServiceAreaAgreementView(view: RegionalServiceAreaAgreementView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
