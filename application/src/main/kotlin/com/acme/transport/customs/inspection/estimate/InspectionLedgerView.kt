package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection ledger. */
data class InspectionLedgerView(
    var code: String,
    var name: String,
    var reviewer: String,
    var notes: String,
    var country: String,
    var updatedAtText: String,
    var priority: String,
    var descriptionText: String,
    var segment: String,
    var categoryText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toInspectionLedgerView(item: InspectionLedger): InspectionLedgerView {
    return InspectionLedgerView(
        code = truncate(item.code, 120),
        name = item.name,
        reviewer = item.reviewer,
        notes = truncate(item.notes, 120),
        country = item.country,
        updatedAtText = item.updatedAt.toString(),
        priority = item.priority.toString(),
        descriptionText = item.description,
        segment = item.segment,
        categoryText = item.category,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionLedgerViews(items: MutableList<InspectionLedger>): MutableList<InspectionLedgerView> {
    val views = mutableListOf<InspectionLedgerView>()
    for (item in items) {
        views.add(toInspectionLedgerView(item))
    }
    return views
}

fun sumInspectionLedgerView(view: InspectionLedgerView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedUpdatedAt.toDouble() + parsedPriority.toDouble() + parsedFloorValue
}
