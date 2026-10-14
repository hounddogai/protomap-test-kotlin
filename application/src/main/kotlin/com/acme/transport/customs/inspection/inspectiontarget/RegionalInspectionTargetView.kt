package com.acme.transport.customs.inspection.inspectiontarget

/** How screens and exports show a regional inspection target. */
data class RegionalInspectionTargetView(
    var code: String,
    var nameText: String,
    var budget: String,
    var endsAt: String,
    var quantity: String,
    var durationMinutes: String,
    var unit: String,
    var statusLabel: String,
)

fun toRegionalInspectionTargetView(item: RegionalInspectionTarget): RegionalInspectionTargetView {
    return RegionalInspectionTargetView(
        code = item.code,
        nameText = item.name,
        budget = "%.2f".format(item.budget),
        endsAt = item.endsAt.toString(),
        quantity = item.quantity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalInspectionTargetViews(items: MutableList<RegionalInspectionTarget>): MutableList<RegionalInspectionTargetView> {
    val views = mutableListOf<RegionalInspectionTargetView>()
    for (item in items) {
        views.add(toRegionalInspectionTargetView(item))
    }
    return views
}

fun sumRegionalInspectionTargetView(view: RegionalInspectionTargetView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedBudget + parsedEndsAt.toDouble() + parsedQuantity.toDouble()
}
