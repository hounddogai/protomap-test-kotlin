package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan slot. */
data class ServicePlanSlotView(
    var code: String,
    var label: String,
    var shiftCode: String,
    var country: String,
    var amount: String,
    var pinned: String,
    var statusLabel: String,
)

fun toServicePlanSlotView(item: ServicePlanSlot): ServicePlanSlotView {
    return ServicePlanSlotView(
        code = item.code,
        label = truncate(item.label, 64),
        shiftCode = item.shiftCode,
        country = truncate(item.country, 120),
        amount = "%.2f".format(item.amount),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanSlotViews(items: MutableList<ServicePlanSlot>): MutableList<ServicePlanSlotView> {
    val views = mutableListOf<ServicePlanSlotView>()
    for (item in items) {
        views.add(toServicePlanSlotView(item))
    }
    return views
}

fun sumServicePlanSlotView(view: ServicePlanSlotView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
