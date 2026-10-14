package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area plan. */
data class ServiceAreaPlanView(
    var code: String,
    var titleText: String,
    var budget: String,
    var rateText: String,
    var channel: String,
    var statusLabel: String,
)

fun toServiceAreaPlanView(item: ServiceAreaPlan): ServiceAreaPlanView {
    return ServiceAreaPlanView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 64),
        budget = "%.2f".format(item.budget),
        rateText = "%.2f".format(item.rate),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaPlanViews(items: MutableList<ServiceAreaPlan>): MutableList<ServiceAreaPlanView> {
    val views = mutableListOf<ServiceAreaPlanView>()
    for (item in items) {
        views.add(toServiceAreaPlanView(item))
    }
    return views
}

fun sumServiceAreaPlanView(view: ServiceAreaPlanView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedBudget + parsedRate
}
