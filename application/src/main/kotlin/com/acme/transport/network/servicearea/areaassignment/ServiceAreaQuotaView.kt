package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area quota. */
data class ServiceAreaQuotaView(
    var codeText: String,
    var titleText: String,
    var categoryText: String,
    var sequence: String,
    var endsAt: String,
    var budget: String,
    var source: String,
    var statusLabel: String,
)

fun toServiceAreaQuotaView(item: ServiceAreaQuota): ServiceAreaQuotaView {
    return ServiceAreaQuotaView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        categoryText = item.category,
        sequence = item.sequence.toString(),
        endsAt = item.endsAt.toString(),
        budget = "%.2f".format(item.budget),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaQuotaViews(items: MutableList<ServiceAreaQuota>): MutableList<ServiceAreaQuotaView> {
    val views = mutableListOf<ServiceAreaQuotaView>()
    for (item in items) {
        views.add(toServiceAreaQuotaView(item))
    }
    return views
}

fun sumServiceAreaQuotaView(view: ServiceAreaQuotaView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedSequence.toDouble() + parsedEndsAt.toDouble() + parsedBudget
}
