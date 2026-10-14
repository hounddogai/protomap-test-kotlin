package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer plan. */
data class TrailerPlanView(
    var codeText: String,
    var title: String,
    var trailerId: String,
    var externalRefText: String,
    var loadKgText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toTrailerPlanView(item: TrailerPlan): TrailerPlanView {
    return TrailerPlanView(
        codeText = item.code,
        title = truncate(item.title, 120),
        trailerId = item.trailerId,
        externalRefText = item.externalRef,
        loadKgText = "%.2f".format(item.loadKg),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerPlanViews(items: MutableList<TrailerPlan>): MutableList<TrailerPlanView> {
    val views = mutableListOf<TrailerPlanView>()
    for (item in items) {
        views.add(toTrailerPlanView(item))
    }
    return views
}

fun sumTrailerPlanView(view: TrailerPlanView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedLoadKg
}
