package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted lane plan agreement. */
data class WeightedLanePlanAgreementView(
    var codeText: String,
    var label: String,
    var externalRef: String,
    var phoneText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toWeightedLanePlanAgreementView(item: WeightedLanePlanAgreement): WeightedLanePlanAgreementView {
    return WeightedLanePlanAgreementView(
        codeText = item.code,
        label = item.label,
        externalRef = truncate(item.externalRef, 40),
        phoneText = item.phone,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedLanePlanAgreementViews(items: MutableList<WeightedLanePlanAgreement>): MutableList<WeightedLanePlanAgreementView> {
    val views = mutableListOf<WeightedLanePlanAgreementView>()
    for (item in items) {
        views.add(toWeightedLanePlanAgreementView(item))
    }
    return views
}

fun sumWeightedLanePlanAgreementView(view: WeightedLanePlanAgreementView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFloorValue
}
