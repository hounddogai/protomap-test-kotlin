package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered declaration assignment. */
data class TieredDeclarationAssignmentView(
    var code: String,
    var labelText: String,
    var depotText: String,
    var limit: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toTieredDeclarationAssignmentView(item: TieredDeclarationAssignment): TieredDeclarationAssignmentView {
    return TieredDeclarationAssignmentView(
        code = truncate(item.code, 80),
        labelText = item.label,
        depotText = item.depot,
        limit = item.limit.toString(),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDeclarationAssignmentViews(items: MutableList<TieredDeclarationAssignment>): MutableList<TieredDeclarationAssignmentView> {
    val views = mutableListOf<TieredDeclarationAssignmentView>()
    for (item in items) {
        views.add(toTieredDeclarationAssignmentView(item))
    }
    return views
}

fun sumTieredDeclarationAssignmentView(view: TieredDeclarationAssignmentView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedLimit.toDouble() + parsedOdometer.toDouble()
}
