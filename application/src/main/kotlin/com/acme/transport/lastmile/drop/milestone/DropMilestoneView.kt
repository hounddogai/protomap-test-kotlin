package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop milestone. */
data class DropMilestoneView(
    var code: String,
    var name: String,
    var routeCode: String,
    var locale: String,
    var label: String,
    var driverId: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDropMilestoneView(item: DropMilestone): DropMilestoneView {
    return DropMilestoneView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        routeCode = item.routeCode,
        locale = truncate(item.locale, 80),
        label = item.label,
        driverId = item.driverId,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropMilestoneViews(items: MutableList<DropMilestone>): MutableList<DropMilestoneView> {
    val views = mutableListOf<DropMilestoneView>()
    for (item in items) {
        views.add(toDropMilestoneView(item))
    }
    return views
}

fun sumDropMilestoneView(view: DropMilestoneView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
