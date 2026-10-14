package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor agreement. */
data class EmissionFactorAgreementView(
    var code: String,
    var title: String,
    var threshold: String,
    var windowDays: String,
    var quantity: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toEmissionFactorAgreementView(item: EmissionFactorAgreement): EmissionFactorAgreementView {
    return EmissionFactorAgreementView(
        code = item.code,
        title = truncate(item.title, 120),
        threshold = "%.2f".format(item.threshold),
        windowDays = item.windowDays.toString(),
        quantity = item.quantity.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorAgreementViews(items: MutableList<EmissionFactorAgreement>): MutableList<EmissionFactorAgreementView> {
    val views = mutableListOf<EmissionFactorAgreementView>()
    for (item in items) {
        views.add(toEmissionFactorAgreementView(item))
    }
    return views
}

fun sumEmissionFactorAgreementView(view: EmissionFactorAgreementView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedThreshold + parsedWindowDays.toDouble() + parsedQuantity.toDouble()
}
