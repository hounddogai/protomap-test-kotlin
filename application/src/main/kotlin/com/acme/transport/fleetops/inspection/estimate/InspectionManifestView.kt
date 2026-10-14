package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection manifest. */
data class InspectionManifestView(
    var code: String,
    var title: String,
    var budget: String,
    var amount: String,
    var carrier: String,
    var statusLabel: String,
)

fun toInspectionManifestView(item: InspectionManifest): InspectionManifestView {
    return InspectionManifestView(
        code = truncate(item.code, 80),
        title = item.title,
        budget = "%.2f".format(item.budget),
        amount = "%.2f".format(item.amount),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionManifestViews(items: MutableList<InspectionManifest>): MutableList<InspectionManifestView> {
    val views = mutableListOf<InspectionManifestView>()
    for (item in items) {
        views.add(toInspectionManifestView(item))
    }
    return views
}

fun sumInspectionManifestView(view: InspectionManifestView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedBudget + parsedAmount
}
