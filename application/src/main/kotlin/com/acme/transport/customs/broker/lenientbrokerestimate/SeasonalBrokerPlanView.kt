package com.acme.transport.customs.broker.lenientbrokerestimate

/** How screens and exports show a seasonal broker plan. */
data class SeasonalBrokerPlanView(
    var code: String,
    var nameText: String,
    var cost: String,
    var archived: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toSeasonalBrokerPlanView(item: SeasonalBrokerPlan): SeasonalBrokerPlanView {
    return SeasonalBrokerPlanView(
        code = item.code,
        nameText = item.name,
        cost = "%.2f".format(item.cost),
        archived = "${item.archived}",
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalBrokerPlanViews(items: MutableList<SeasonalBrokerPlan>): MutableList<SeasonalBrokerPlanView> {
    val views = mutableListOf<SeasonalBrokerPlanView>()
    for (item in items) {
        views.add(toSeasonalBrokerPlanView(item))
    }
    return views
}

fun sumSeasonalBrokerPlanView(view: SeasonalBrokerPlanView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCost + parsedEtaMinutes.toDouble()
}
