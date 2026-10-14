package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary coaching mapping. */
data class PrimaryCoachingMappingView(
    var codeText: String,
    var title: String,
    var budget: String,
    var score: String,
    var statusLabel: String,
)

fun toPrimaryCoachingMappingView(item: PrimaryCoachingMapping): PrimaryCoachingMappingView {
    return PrimaryCoachingMappingView(
        codeText = truncate(item.code, 120),
        title = item.title,
        budget = "%.2f".format(item.budget),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCoachingMappingViews(items: MutableList<PrimaryCoachingMapping>): MutableList<PrimaryCoachingMappingView> {
    val views = mutableListOf<PrimaryCoachingMappingView>()
    for (item in items) {
        views.add(toPrimaryCoachingMappingView(item))
    }
    return views
}

fun sumPrimaryCoachingMappingView(view: PrimaryCoachingMappingView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedBudget + parsedScore
}
