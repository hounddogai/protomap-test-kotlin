package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan template. */
data class ServicePlanTemplateView(
    var code: String,
    var nameText: String,
    var amount: String,
    var cost: String,
    var capacityText: String,
    var threshold: String,
    var dock: String,
    var phone: String,
    var ratioText: String,
    var pinned: String,
    var budget: String,
    var statusLabel: String,
)

fun toServicePlanTemplateView(item: ServicePlanTemplate): ServicePlanTemplateView {
    return ServicePlanTemplateView(
        code = item.code,
        nameText = truncate(item.name, 120),
        amount = "%.2f".format(item.amount),
        cost = "%.2f".format(item.cost),
        capacityText = item.capacity.toString(),
        threshold = "%.2f".format(item.threshold),
        dock = item.dock,
        phone = item.phone,
        ratioText = "%.2f".format(item.ratio),
        pinned = "${item.pinned}",
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanTemplateViews(items: MutableList<ServicePlanTemplate>): MutableList<ServicePlanTemplateView> {
    val views = mutableListOf<ServicePlanTemplateView>()
    for (item in items) {
        views.add(toServicePlanTemplateView(item))
    }
    return views
}

fun sumServicePlanTemplateView(view: ServicePlanTemplateView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedAmount + parsedCost + parsedCapacity.toDouble()
}
