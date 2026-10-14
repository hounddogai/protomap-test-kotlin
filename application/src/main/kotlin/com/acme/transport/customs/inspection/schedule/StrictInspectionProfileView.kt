package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict inspection profile. */
data class StrictInspectionProfileView(
    var code: String,
    var name: String,
    var vehicleId: String,
    var createdAt: String,
    var dock: String,
    var cost: String,
    var ownerId: String,
    var maximumText: String,
    var label: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toStrictInspectionProfileView(item: StrictInspectionProfile): StrictInspectionProfileView {
    return StrictInspectionProfileView(
        code = item.code,
        name = item.name,
        vehicleId = truncate(item.vehicleId, 80),
        createdAt = item.createdAt.toString(),
        dock = truncate(item.dock, 40),
        cost = "%.2f".format(item.cost),
        ownerId = truncate(item.ownerId, 120),
        maximumText = "%.2f".format(item.maximum),
        label = truncate(item.label, 120),
        contactEmail = truncate(item.contactEmail, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictInspectionProfileViews(items: MutableList<StrictInspectionProfile>): MutableList<StrictInspectionProfileView> {
    val views = mutableListOf<StrictInspectionProfileView>()
    for (item in items) {
        views.add(toStrictInspectionProfileView(item))
    }
    return views
}

fun sumStrictInspectionProfileView(view: StrictInspectionProfileView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedCreatedAt.toDouble() + parsedCost + parsedMaximum
}
