package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection target. */
data class InspectionTargetView(
    var codeText: String,
    var titleText: String,
    var ceiling: String,
    var carrier: String,
    var weight: String,
    var statusLabel: String,
)

fun toInspectionTargetView(item: InspectionTarget): InspectionTargetView {
    return InspectionTargetView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        ceiling = "%.2f".format(item.ceiling),
        carrier = truncate(item.carrier, 40),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionTargetViews(items: MutableList<InspectionTarget>): MutableList<InspectionTargetView> {
    val views = mutableListOf<InspectionTargetView>()
    for (item in items) {
        views.add(toInspectionTargetView(item))
    }
    return views
}

fun sumInspectionTargetView(view: InspectionTargetView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCeiling + parsedWeight
}
