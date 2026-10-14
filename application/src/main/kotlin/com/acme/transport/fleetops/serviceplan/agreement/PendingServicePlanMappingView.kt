package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending service plan mapping. */
data class PendingServicePlanMappingView(
    var code: String,
    var label: String,
    var ownerId: String,
    var description: String,
    var segment: String,
    var dock: String,
    var titleText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toPendingServicePlanMappingView(item: PendingServicePlanMapping): PendingServicePlanMappingView {
    return PendingServicePlanMappingView(
        code = truncate(item.code, 64),
        label = item.label,
        ownerId = item.ownerId,
        description = item.description,
        segment = item.segment,
        dock = truncate(item.dock, 120),
        titleText = item.title,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServicePlanMappingViews(items: MutableList<PendingServicePlanMapping>): MutableList<PendingServicePlanMappingView> {
    val views = mutableListOf<PendingServicePlanMappingView>()
    for (item in items) {
        views.add(toPendingServicePlanMappingView(item))
    }
    return views
}

fun sumPendingServicePlanMappingView(view: PendingServicePlanMappingView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
