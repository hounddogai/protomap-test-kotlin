package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area adjustment. */
data class ServiceAreaAdjustmentView(
    var code: String,
    var title: String,
    var budget: String,
    var visible: String,
    var limitText: String,
    var discount: String,
    var expiresAtText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toServiceAreaAdjustmentView(item: ServiceAreaAdjustment): ServiceAreaAdjustmentView {
    return ServiceAreaAdjustmentView(
        code = truncate(item.code, 64),
        title = item.title,
        budget = "%.2f".format(item.budget),
        visible = "${item.visible}",
        limitText = item.limit.toString(),
        discount = "%.2f".format(item.discount),
        expiresAtText = item.expiresAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaAdjustmentViews(items: MutableList<ServiceAreaAdjustment>): MutableList<ServiceAreaAdjustmentView> {
    val views = mutableListOf<ServiceAreaAdjustmentView>()
    for (item in items) {
        views.add(toServiceAreaAdjustmentView(item))
    }
    return views
}

fun sumServiceAreaAdjustmentView(view: ServiceAreaAdjustmentView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedBudget + parsedLimit.toDouble() + parsedDiscount
}
