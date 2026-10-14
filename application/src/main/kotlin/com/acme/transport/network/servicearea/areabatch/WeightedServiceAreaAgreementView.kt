package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted service area agreement. */
data class WeightedServiceAreaAgreementView(
    var code: String,
    var name: String,
    var windowDaysText: String,
    var ceilingText: String,
    var revisionText: String,
    var locale: String,
    var statusLabel: String,
)

fun toWeightedServiceAreaAgreementView(item: WeightedServiceAreaAgreement): WeightedServiceAreaAgreementView {
    return WeightedServiceAreaAgreementView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 120),
        windowDaysText = item.windowDays.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        revisionText = item.revision.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedServiceAreaAgreementViews(items: MutableList<WeightedServiceAreaAgreement>): MutableList<WeightedServiceAreaAgreementView> {
    val views = mutableListOf<WeightedServiceAreaAgreementView>()
    for (item in items) {
        views.add(toWeightedServiceAreaAgreementView(item))
    }
    return views
}

fun sumWeightedServiceAreaAgreementView(view: WeightedServiceAreaAgreementView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedWindowDays.toDouble() + parsedCeiling + parsedRevision.toDouble()
}
