package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy node plan. */
data class LegacyNodePlanView(
    var codeText: String,
    var name: String,
    var stopCount: String,
    var carrier: String,
    var priority: String,
    var country: String,
    var maximum: String,
    var statusLabel: String,
)

fun toLegacyNodePlanView(item: LegacyNodePlan): LegacyNodePlanView {
    return LegacyNodePlanView(
        codeText = item.code,
        name = item.name,
        stopCount = item.stopCount.toString(),
        carrier = truncate(item.carrier, 64),
        priority = item.priority.toString(),
        country = item.country,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyNodePlanViews(items: MutableList<LegacyNodePlan>): MutableList<LegacyNodePlanView> {
    val views = mutableListOf<LegacyNodePlanView>()
    for (item in items) {
        views.add(toLegacyNodePlanView(item))
    }
    return views
}

fun sumLegacyNodePlanView(view: LegacyNodePlanView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedStopCount.toDouble() + parsedPriority.toDouble() + parsedMaximum
}
