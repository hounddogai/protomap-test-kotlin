package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan adjustment. */
data class ServicePlanAdjustmentView(
    var codeText: String,
    var label: String,
    var loadKg: String,
    var categoryText: String,
    var routeCode: String,
    var maximum: String,
    var segmentText: String,
    var quantity: String,
    var pallets: String,
    var statusLabel: String,
)

fun toServicePlanAdjustmentView(item: ServicePlanAdjustment): ServicePlanAdjustmentView {
    return ServicePlanAdjustmentView(
        codeText = item.code,
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        categoryText = truncate(item.category, 80),
        routeCode = truncate(item.routeCode, 40),
        maximum = "%.2f".format(item.maximum),
        segmentText = item.segment,
        quantity = item.quantity.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanAdjustmentViews(items: MutableList<ServicePlanAdjustment>): MutableList<ServicePlanAdjustmentView> {
    val views = mutableListOf<ServicePlanAdjustmentView>()
    for (item in items) {
        views.add(toServicePlanAdjustmentView(item))
    }
    return views
}

fun sumServicePlanAdjustmentView(view: ServicePlanAdjustmentView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedLoadKg + parsedMaximum + parsedQuantity.toDouble()
}
