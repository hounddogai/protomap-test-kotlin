package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan rule. */
data class ServicePlanRuleView(
    var code: String,
    var label: String,
    var score: String,
    var laneText: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toServicePlanRuleView(item: ServicePlanRule): ServicePlanRuleView {
    return ServicePlanRuleView(
        code = item.code,
        label = truncate(item.label, 64),
        score = "%.2f".format(item.score),
        laneText = item.lane,
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanRuleViews(items: MutableList<ServicePlanRule>): MutableList<ServicePlanRuleView> {
    val views = mutableListOf<ServicePlanRuleView>()
    for (item in items) {
        views.add(toServicePlanRuleView(item))
    }
    return views
}

fun sumServicePlanRuleView(view: ServicePlanRuleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedScore + parsedEndsAt.toDouble()
}
