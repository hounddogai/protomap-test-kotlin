package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break plan. */
data class RestBreakPlanView(
    var code: String,
    var name: String,
    var expiresAt: String,
    var timezone: String,
    var archived: String,
    var ratioText: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toRestBreakPlanView(item: RestBreakPlan): RestBreakPlanView {
    return RestBreakPlanView(
        code = item.code,
        name = item.name,
        expiresAt = item.expiresAt.toString(),
        timezone = item.timezone,
        archived = "${item.archived}",
        ratioText = "%.2f".format(item.ratio),
        sourceText = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakPlanViews(items: MutableList<RestBreakPlan>): MutableList<RestBreakPlanView> {
    val views = mutableListOf<RestBreakPlanView>()
    for (item in items) {
        views.add(toRestBreakPlanView(item))
    }
    return views
}

fun sumRestBreakPlanView(view: RestBreakPlanView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedExpiresAt.toDouble() + parsedRatio
}
