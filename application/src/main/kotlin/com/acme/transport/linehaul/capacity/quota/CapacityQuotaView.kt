package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity quota. */
data class CapacityQuotaView(
    var code: String,
    var title: String,
    var budget: String,
    var segment: String,
    var contactEmail: String,
    var channel: String,
    var thresholdText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toCapacityQuotaView(item: CapacityQuota): CapacityQuotaView {
    return CapacityQuotaView(
        code = truncate(item.code, 120),
        title = item.title,
        budget = "%.2f".format(item.budget),
        segment = item.segment,
        contactEmail = item.contactEmail,
        channel = item.channel,
        thresholdText = "%.2f".format(item.threshold),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityQuotaViews(items: MutableList<CapacityQuota>): MutableList<CapacityQuotaView> {
    val views = mutableListOf<CapacityQuotaView>()
    for (item in items) {
        views.add(toCapacityQuotaView(item))
    }
    return views
}

fun sumCapacityQuotaView(view: CapacityQuotaView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedBudget + parsedThreshold + parsedFuelLiters
}
