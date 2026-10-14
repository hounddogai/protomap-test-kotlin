package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy utilization segment. */
data class LegacyUtilizationSegmentView(
    var codeText: String,
    var name: String,
    var startsAt: String,
    var active: String,
    var discount: String,
    var quantity: String,
    var description: String,
    var limitText: String,
    var labelText: String,
    var statusLabel: String,
)

fun toLegacyUtilizationSegmentView(item: LegacyUtilizationSegment): LegacyUtilizationSegmentView {
    return LegacyUtilizationSegmentView(
        codeText = item.code,
        name = truncate(item.name, 64),
        startsAt = item.startsAt.toString(),
        active = "${item.active}",
        discount = "%.2f".format(item.discount),
        quantity = item.quantity.toString(),
        description = truncate(item.description, 120),
        limitText = item.limit.toString(),
        labelText = truncate(item.label, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyUtilizationSegmentViews(items: MutableList<LegacyUtilizationSegment>): MutableList<LegacyUtilizationSegmentView> {
    val views = mutableListOf<LegacyUtilizationSegmentView>()
    for (item in items) {
        views.add(toLegacyUtilizationSegmentView(item))
    }
    return views
}

fun sumLegacyUtilizationSegmentView(view: LegacyUtilizationSegmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedStartsAt.toDouble() + parsedDiscount + parsedQuantity.toDouble()
}
