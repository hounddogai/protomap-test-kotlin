package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection baseline. */
data class InspectionBaselineView(
    var codeText: String,
    var labelText: String,
    var approved: String,
    var routeCodeText: String,
    var visibleText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toInspectionBaselineView(item: InspectionBaseline): InspectionBaselineView {
    return InspectionBaselineView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        approved = "${item.approved}",
        routeCodeText = item.routeCode,
        visibleText = "${item.visible}",
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionBaselineViews(items: MutableList<InspectionBaseline>): MutableList<InspectionBaselineView> {
    val views = mutableListOf<InspectionBaselineView>()
    for (item in items) {
        views.add(toInspectionBaselineView(item))
    }
    return views
}

fun sumInspectionBaselineView(view: InspectionBaselineView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
