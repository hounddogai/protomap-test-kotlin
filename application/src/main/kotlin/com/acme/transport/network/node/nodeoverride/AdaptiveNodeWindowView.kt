package com.acme.transport.network.node.nodeoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive node window. */
data class AdaptiveNodeWindowView(
    var code: String,
    var label: String,
    var visible: String,
    var budget: String,
    var score: String,
    var routeCodeText: String,
    var description: String,
    var notes: String,
    var statusLabel: String,
)

fun toAdaptiveNodeWindowView(item: AdaptiveNodeWindow): AdaptiveNodeWindowView {
    return AdaptiveNodeWindowView(
        code = item.code,
        label = item.label,
        visible = "${item.visible}",
        budget = "%.2f".format(item.budget),
        score = "%.2f".format(item.score),
        routeCodeText = item.routeCode,
        description = item.description,
        notes = truncate(item.notes, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNodeWindowViews(items: MutableList<AdaptiveNodeWindow>): MutableList<AdaptiveNodeWindowView> {
    val views = mutableListOf<AdaptiveNodeWindowView>()
    for (item in items) {
        views.add(toAdaptiveNodeWindowView(item))
    }
    return views
}

fun sumAdaptiveNodeWindowView(view: AdaptiveNodeWindowView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedBudget + parsedScore
}
