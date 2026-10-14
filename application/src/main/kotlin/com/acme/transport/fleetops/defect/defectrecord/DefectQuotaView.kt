package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect quota. */
data class DefectQuotaView(
    var code: String,
    var title: String,
    var budget: String,
    var shiftCode: String,
    var minimumText: String,
    var contactEmailText: String,
    var capacityText: String,
    var amount: String,
    var labelText: String,
    var source: String,
    var statusLabel: String,
)

fun toDefectQuotaView(item: DefectQuota): DefectQuotaView {
    return DefectQuotaView(
        code = item.code,
        title = item.title,
        budget = "%.2f".format(item.budget),
        shiftCode = truncate(item.shiftCode, 80),
        minimumText = "%.2f".format(item.minimum),
        contactEmailText = truncate(item.contactEmail, 40),
        capacityText = item.capacity.toString(),
        amount = "%.2f".format(item.amount),
        labelText = item.label,
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectQuotaViews(items: MutableList<DefectQuota>): MutableList<DefectQuotaView> {
    val views = mutableListOf<DefectQuotaView>()
    for (item in items) {
        views.add(toDefectQuotaView(item))
    }
    return views
}

fun sumDefectQuotaView(view: DefectQuotaView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedBudget + parsedMinimum + parsedCapacity.toDouble()
}
