package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled service plan tier. */
data class ScheduledServicePlanTierView(
    var code: String,
    var titleText: String,
    var summaryText: String,
    var limit: String,
    var quantityText: String,
    var depotText: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toScheduledServicePlanTierView(item: ScheduledServicePlanTier): ScheduledServicePlanTierView {
    return ScheduledServicePlanTierView(
        code = item.code,
        titleText = item.title,
        summaryText = item.summary,
        limit = item.limit.toString(),
        quantityText = item.quantity.toString(),
        depotText = truncate(item.depot, 40),
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledServicePlanTierViews(items: MutableList<ScheduledServicePlanTier>): MutableList<ScheduledServicePlanTierView> {
    val views = mutableListOf<ScheduledServicePlanTierView>()
    for (item in items) {
        views.add(toScheduledServicePlanTierView(item))
    }
    return views
}

fun sumScheduledServicePlanTierView(view: ScheduledServicePlanTierView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedLimit.toDouble() + parsedQuantity.toDouble()
}
