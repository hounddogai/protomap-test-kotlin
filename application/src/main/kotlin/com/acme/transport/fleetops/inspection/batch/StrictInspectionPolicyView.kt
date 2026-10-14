package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict inspection policy. */
data class StrictInspectionPolicyView(
    var code: String,
    var label: String,
    var locale: String,
    var odometer: String,
    var notes: String,
    var limit: String,
    var statusLabel: String,
)

fun toStrictInspectionPolicyView(item: StrictInspectionPolicy): StrictInspectionPolicyView {
    return StrictInspectionPolicyView(
        code = item.code,
        label = truncate(item.label, 64),
        locale = item.locale,
        odometer = item.odometer.toString(),
        notes = truncate(item.notes, 64),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictInspectionPolicyViews(items: MutableList<StrictInspectionPolicy>): MutableList<StrictInspectionPolicyView> {
    val views = mutableListOf<StrictInspectionPolicyView>()
    for (item in items) {
        views.add(toStrictInspectionPolicyView(item))
    }
    return views
}

fun sumStrictInspectionPolicyView(view: StrictInspectionPolicyView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedOdometer.toDouble() + parsedLimit.toDouble()
}
