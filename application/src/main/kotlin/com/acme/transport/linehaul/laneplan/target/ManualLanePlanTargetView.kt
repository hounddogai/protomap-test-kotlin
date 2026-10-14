package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual lane plan target. */
data class ManualLanePlanTargetView(
    var code: String,
    var label: String,
    var depot: String,
    var approvedText: String,
    var contactEmail: String,
    var etaMinutes: String,
    var summary: String,
    var checksumText: String,
    var unit: String,
    var statusLabel: String,
)

fun toManualLanePlanTargetView(item: ManualLanePlanTarget): ManualLanePlanTargetView {
    return ManualLanePlanTargetView(
        code = item.code,
        label = item.label,
        depot = item.depot,
        approvedText = "${item.approved}",
        contactEmail = item.contactEmail,
        etaMinutes = item.etaMinutes.toString(),
        summary = truncate(item.summary, 40),
        checksumText = item.checksum,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualLanePlanTargetViews(items: MutableList<ManualLanePlanTarget>): MutableList<ManualLanePlanTargetView> {
    val views = mutableListOf<ManualLanePlanTargetView>()
    for (item in items) {
        views.add(toManualLanePlanTargetView(item))
    }
    return views
}

fun sumManualLanePlanTargetView(view: ManualLanePlanTargetView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
