package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy defect agreement. */
data class LegacyDefectAgreementView(
    var code: String,
    var name: String,
    var currency: String,
    var marginText: String,
    var locale: String,
    var odometer: String,
    var carrier: String,
    var category: String,
    var sequence: String,
    var statusLabel: String,
)

fun toLegacyDefectAgreementView(item: LegacyDefectAgreement): LegacyDefectAgreementView {
    return LegacyDefectAgreementView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        currency = truncate(item.currency, 80),
        marginText = "%.2f".format(item.margin),
        locale = item.locale,
        odometer = item.odometer.toString(),
        carrier = item.carrier,
        category = item.category,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDefectAgreementViews(items: MutableList<LegacyDefectAgreement>): MutableList<LegacyDefectAgreementView> {
    val views = mutableListOf<LegacyDefectAgreementView>()
    for (item in items) {
        views.add(toLegacyDefectAgreementView(item))
    }
    return views
}

fun sumLegacyDefectAgreementView(view: LegacyDefectAgreementView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedMargin + parsedOdometer.toDouble() + parsedSequence.toDouble()
}
