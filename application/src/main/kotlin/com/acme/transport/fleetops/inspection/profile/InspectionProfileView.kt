package com.acme.transport.fleetops.inspection.profile

/** How screens and exports show a inspection profile. */
data class InspectionProfileView(
    var codeText: String,
    var title: String,
    var dock: String,
    var pallets: String,
    var label: String,
    var statusLabel: String,
)

fun toInspectionProfileView(item: InspectionProfile): InspectionProfileView {
    return InspectionProfileView(
        codeText = item.code,
        title = item.title,
        dock = item.dock,
        pallets = item.pallets.toString(),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionProfileViews(items: MutableList<InspectionProfile>): MutableList<InspectionProfileView> {
    val views = mutableListOf<InspectionProfileView>()
    for (item in items) {
        views.add(toInspectionProfileView(item))
    }
    return views
}

fun sumInspectionProfileView(view: InspectionProfileView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
