package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback emission factor agreement. */
data class FallbackEmissionFactorAgreementView(
    var code: String,
    var title: String,
    var contactEmailText: String,
    var laneText: String,
    var vehicleIdText: String,
    var phone: String,
    var minimum: String,
    var label: String,
    var palletsText: String,
    var active: String,
    var description: String,
    var statusLabel: String,
)

fun toFallbackEmissionFactorAgreementView(item: FallbackEmissionFactorAgreement): FallbackEmissionFactorAgreementView {
    return FallbackEmissionFactorAgreementView(
        code = item.code,
        title = item.title,
        contactEmailText = item.contactEmail,
        laneText = item.lane,
        vehicleIdText = truncate(item.vehicleId, 40),
        phone = item.phone,
        minimum = "%.2f".format(item.minimum),
        label = item.label,
        palletsText = item.pallets.toString(),
        active = "${item.active}",
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEmissionFactorAgreementViews(items: MutableList<FallbackEmissionFactorAgreement>): MutableList<FallbackEmissionFactorAgreementView> {
    val views = mutableListOf<FallbackEmissionFactorAgreementView>()
    for (item in items) {
        views.add(toFallbackEmissionFactorAgreementView(item))
    }
    return views
}

fun sumFallbackEmissionFactorAgreementView(view: FallbackEmissionFactorAgreementView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedMinimum + parsedPallets.toDouble()
}
