package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global inspection template. */
data class GlobalInspectionTemplateView(
    var code: String,
    var name: String,
    var countryText: String,
    var floorValue: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toGlobalInspectionTemplateView(item: GlobalInspectionTemplate): GlobalInspectionTemplateView {
    return GlobalInspectionTemplateView(
        code = truncate(item.code, 80),
        name = item.name,
        countryText = item.country,
        floorValue = "%.2f".format(item.floorValue),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionTemplateViews(items: MutableList<GlobalInspectionTemplate>): MutableList<GlobalInspectionTemplateView> {
    val views = mutableListOf<GlobalInspectionTemplateView>()
    for (item in items) {
        views.add(toGlobalInspectionTemplateView(item))
    }
    return views
}

fun sumGlobalInspectionTemplateView(view: GlobalInspectionTemplateView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFloorValue
}
