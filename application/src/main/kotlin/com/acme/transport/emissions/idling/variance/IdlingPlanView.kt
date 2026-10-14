package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling plan. */
data class IdlingPlanView(
    var code: String,
    var title: String,
    var limitText: String,
    var thresholdText: String,
    var ceilingText: String,
    var category: String,
    var statusLabel: String,
)

fun toIdlingPlanView(item: IdlingPlan): IdlingPlanView {
    return IdlingPlanView(
        code = item.code,
        title = item.title,
        limitText = item.limit.toString(),
        thresholdText = "%.2f".format(item.threshold),
        ceilingText = "%.2f".format(item.ceiling),
        category = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingPlanViews(items: MutableList<IdlingPlan>): MutableList<IdlingPlanView> {
    val views = mutableListOf<IdlingPlanView>()
    for (item in items) {
        views.add(toIdlingPlanView(item))
    }
    return views
}

fun sumIdlingPlanView(view: IdlingPlanView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedLimit.toDouble() + parsedThreshold + parsedCeiling
}
