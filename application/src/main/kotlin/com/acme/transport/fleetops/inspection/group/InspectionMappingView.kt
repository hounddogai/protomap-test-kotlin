package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection mapping. */
data class InspectionMappingView(
    var code: String,
    var name: String,
    var notes: String,
    var approved: String,
    var priority: String,
    var category: String,
    var locale: String,
    var statusLabel: String,
)

fun toInspectionMappingView(item: InspectionMapping): InspectionMappingView {
    return InspectionMappingView(
        code = truncate(item.code, 64),
        name = item.name,
        notes = item.notes,
        approved = "${item.approved}",
        priority = item.priority.toString(),
        category = item.category,
        locale = truncate(item.locale, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionMappingViews(items: MutableList<InspectionMapping>): MutableList<InspectionMappingView> {
    val views = mutableListOf<InspectionMappingView>()
    for (item in items) {
        views.add(toInspectionMappingView(item))
    }
    return views
}

fun sumInspectionMappingView(view: InspectionMappingView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
