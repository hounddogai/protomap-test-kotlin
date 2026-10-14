package com.acme.transport.terminal.traileryard.exception

/** How screens and exports show a trailer yard plan. */
data class TrailerYardPlanView(
    var code: String,
    var labelText: String,
    var startsAtText: String,
    var dock: String,
    var localeText: String,
    var statusLabel: String,
)

fun toTrailerYardPlanView(item: TrailerYardPlan): TrailerYardPlanView {
    return TrailerYardPlanView(
        code = item.code,
        labelText = item.label,
        startsAtText = item.startsAt.toString(),
        dock = item.dock,
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardPlanViews(items: MutableList<TrailerYardPlan>): MutableList<TrailerYardPlanView> {
    val views = mutableListOf<TrailerYardPlanView>()
    for (item in items) {
        views.add(toTrailerYardPlanView(item))
    }
    return views
}

fun sumTrailerYardPlanView(view: TrailerYardPlanView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedStartsAt.toDouble()
}
