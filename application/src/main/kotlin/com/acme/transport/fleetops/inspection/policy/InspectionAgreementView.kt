package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection agreement. */
data class InspectionAgreementView(
    var codeText: String,
    var label: String,
    var description: String,
    var thresholdText: String,
    var rate: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toInspectionAgreementView(item: InspectionAgreement): InspectionAgreementView {
    return InspectionAgreementView(
        codeText = truncate(item.code, 64),
        label = truncate(item.label, 40),
        description = item.description,
        thresholdText = "%.2f".format(item.threshold),
        rate = "%.2f".format(item.rate),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionAgreementViews(items: MutableList<InspectionAgreement>): MutableList<InspectionAgreementView> {
    val views = mutableListOf<InspectionAgreementView>()
    for (item in items) {
        views.add(toInspectionAgreementView(item))
    }
    return views
}

fun sumInspectionAgreementView(view: InspectionAgreementView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedThreshold + parsedRate + parsedPallets.toDouble()
}
