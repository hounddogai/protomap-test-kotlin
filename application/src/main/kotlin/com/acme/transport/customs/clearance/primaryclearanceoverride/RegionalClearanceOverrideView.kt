package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional clearance override. */
data class RegionalClearanceOverrideView(
    var codeText: String,
    var label: String,
    var ceiling: String,
    var notes: String,
    var category: String,
    var weight: String,
    var statusLabel: String,
)

fun toRegionalClearanceOverrideView(item: RegionalClearanceOverride): RegionalClearanceOverrideView {
    return RegionalClearanceOverrideView(
        codeText = truncate(item.code, 120),
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        notes = item.notes,
        category = truncate(item.category, 80),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalClearanceOverrideViews(items: MutableList<RegionalClearanceOverride>): MutableList<RegionalClearanceOverrideView> {
    val views = mutableListOf<RegionalClearanceOverrideView>()
    for (item in items) {
        views.add(toRegionalClearanceOverrideView(item))
    }
    return views
}

fun sumRegionalClearanceOverrideView(view: RegionalClearanceOverrideView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCeiling + parsedWeight
}
