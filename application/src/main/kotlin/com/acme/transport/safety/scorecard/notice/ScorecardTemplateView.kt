package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard template. */
data class ScorecardTemplateView(
    var code: String,
    var title: String,
    var priority: String,
    var approved: String,
    var weightText: String,
    var etaMinutes: String,
    var startsAt: String,
    var notes: String,
    var statusLabel: String,
)

fun toScorecardTemplateView(item: ScorecardTemplate): ScorecardTemplateView {
    return ScorecardTemplateView(
        code = item.code,
        title = truncate(item.title, 120),
        priority = item.priority.toString(),
        approved = "${item.approved}",
        weightText = "%.2f".format(item.weight),
        etaMinutes = item.etaMinutes.toString(),
        startsAt = item.startsAt.toString(),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardTemplateViews(items: MutableList<ScorecardTemplate>): MutableList<ScorecardTemplateView> {
    val views = mutableListOf<ScorecardTemplateView>()
    for (item in items) {
        views.add(toScorecardTemplateView(item))
    }
    return views
}

fun sumScorecardTemplateView(view: ScorecardTemplateView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedPriority.toDouble() + parsedWeight + parsedEtaMinutes.toDouble()
}
