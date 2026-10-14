package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization agreement. */
data class UtilizationAgreementView(
    var code: String,
    var title: String,
    var loadKg: String,
    var version: String,
    var unit: String,
    var statusLabel: String,
)

fun toUtilizationAgreementView(item: UtilizationAgreement): UtilizationAgreementView {
    return UtilizationAgreementView(
        code = item.code,
        title = item.title,
        loadKg = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        unit = truncate(item.unit, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationAgreementViews(items: MutableList<UtilizationAgreement>): MutableList<UtilizationAgreementView> {
    val views = mutableListOf<UtilizationAgreementView>()
    for (item in items) {
        views.add(toUtilizationAgreementView(item))
    }
    return views
}

fun sumUtilizationAgreementView(view: UtilizationAgreementView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedLoadKg + parsedVersion.toDouble()
}
