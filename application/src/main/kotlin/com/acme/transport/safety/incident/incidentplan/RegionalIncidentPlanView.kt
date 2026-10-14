package com.acme.transport.safety.incident.incidentplan

/** How screens and exports show a regional incident plan. */
data class RegionalIncidentPlanView(
    var code: String,
    var title: String,
    var externalRef: String,
    var category: String,
    var dock: String,
    var reviewer: String,
    var descriptionText: String,
    var score: String,
    var statusLabel: String,
)

fun toRegionalIncidentPlanView(item: RegionalIncidentPlan): RegionalIncidentPlanView {
    return RegionalIncidentPlanView(
        code = item.code,
        title = item.title,
        externalRef = item.externalRef,
        category = item.category,
        dock = item.dock,
        reviewer = item.reviewer,
        descriptionText = item.description,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIncidentPlanViews(items: MutableList<RegionalIncidentPlan>): MutableList<RegionalIncidentPlanView> {
    val views = mutableListOf<RegionalIncidentPlanView>()
    for (item in items) {
        views.add(toRegionalIncidentPlanView(item))
    }
    return views
}

fun sumRegionalIncidentPlanView(view: RegionalIncidentPlanView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
