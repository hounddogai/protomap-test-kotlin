package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict availability agreement. */
data class StrictAvailabilityAgreementView(
    var codeText: String,
    var name: String,
    var region: String,
    var segment: String,
    var dock: String,
    var checksumText: String,
    var amount: String,
    var statusLabel: String,
)

fun toStrictAvailabilityAgreementView(item: StrictAvailabilityAgreement): StrictAvailabilityAgreementView {
    return StrictAvailabilityAgreementView(
        codeText = item.code,
        name = item.name,
        region = truncate(item.region, 64),
        segment = item.segment,
        dock = truncate(item.dock, 80),
        checksumText = truncate(item.checksum, 64),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAvailabilityAgreementViews(items: MutableList<StrictAvailabilityAgreement>): MutableList<StrictAvailabilityAgreementView> {
    val views = mutableListOf<StrictAvailabilityAgreementView>()
    for (item in items) {
        views.add(toStrictAvailabilityAgreementView(item))
    }
    return views
}

fun sumStrictAvailabilityAgreementView(view: StrictAvailabilityAgreementView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
