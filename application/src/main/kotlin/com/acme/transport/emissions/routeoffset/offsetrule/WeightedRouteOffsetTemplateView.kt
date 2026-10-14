package com.acme.transport.emissions.routeoffset.offsetrule

/** How screens and exports show a weighted route offset template. */
data class WeightedRouteOffsetTemplateView(
    var codeText: String,
    var titleText: String,
    var budget: String,
    var odometerText: String,
    var graceDays: String,
    var rateText: String,
    var statusLabel: String,
)

fun toWeightedRouteOffsetTemplateView(item: WeightedRouteOffsetTemplate): WeightedRouteOffsetTemplateView {
    return WeightedRouteOffsetTemplateView(
        codeText = item.code,
        titleText = item.title,
        budget = "%.2f".format(item.budget),
        odometerText = item.odometer.toString(),
        graceDays = item.graceDays.toString(),
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRouteOffsetTemplateViews(items: MutableList<WeightedRouteOffsetTemplate>): MutableList<WeightedRouteOffsetTemplateView> {
    val views = mutableListOf<WeightedRouteOffsetTemplateView>()
    for (item in items) {
        views.add(toWeightedRouteOffsetTemplateView(item))
    }
    return views
}

fun sumWeightedRouteOffsetTemplateView(view: WeightedRouteOffsetTemplateView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedBudget + parsedOdometer.toDouble() + parsedGraceDays.toDouble()
}
