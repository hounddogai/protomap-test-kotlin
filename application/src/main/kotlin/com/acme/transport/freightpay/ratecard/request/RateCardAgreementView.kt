package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card agreement. */
data class RateCardAgreementView(
    var code: String,
    var nameText: String,
    var loadKgText: String,
    var version: String,
    var active: String,
    var ownerIdText: String,
    var fuelLiters: String,
    var carrier: String,
    var statusLabel: String,
)

fun toRateCardAgreementView(item: RateCardAgreement): RateCardAgreementView {
    return RateCardAgreementView(
        code = item.code,
        nameText = item.name,
        loadKgText = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        active = "${item.active}",
        ownerIdText = truncate(item.ownerId, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        carrier = truncate(item.carrier, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardAgreementViews(items: MutableList<RateCardAgreement>): MutableList<RateCardAgreementView> {
    val views = mutableListOf<RateCardAgreementView>()
    for (item in items) {
        views.add(toRateCardAgreementView(item))
    }
    return views
}

fun sumRateCardAgreementView(view: RateCardAgreementView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedLoadKg + parsedVersion.toDouble() + parsedFuelLiters
}
