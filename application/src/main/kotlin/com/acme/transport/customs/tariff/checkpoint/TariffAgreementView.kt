package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff agreement. */
data class TariffAgreementView(
    var codeText: String,
    var labelText: String,
    var discountText: String,
    var pinned: String,
    var maximumText: String,
    var carrier: String,
    var approved: String,
    var cost: String,
    var laneText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toTariffAgreementView(item: TariffAgreement): TariffAgreementView {
    return TariffAgreementView(
        codeText = truncate(item.code, 64),
        labelText = truncate(item.label, 120),
        discountText = "%.2f".format(item.discount),
        pinned = "${item.pinned}",
        maximumText = "%.2f".format(item.maximum),
        carrier = item.carrier,
        approved = "${item.approved}",
        cost = "%.2f".format(item.cost),
        laneText = truncate(item.lane, 80),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffAgreementViews(items: MutableList<TariffAgreement>): MutableList<TariffAgreementView> {
    val views = mutableListOf<TariffAgreementView>()
    for (item in items) {
        views.add(toTariffAgreementView(item))
    }
    return views
}

fun sumTariffAgreementView(view: TariffAgreementView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedDiscount + parsedMaximum + parsedCost
}
