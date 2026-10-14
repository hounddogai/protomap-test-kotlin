package com.acme.transport.customs.broker.adjustment

/** How screens and exports show a legacy broker plan. */
data class LegacyBrokerPlanView(
    var code: String,
    var name: String,
    var maximumText: String,
    var distanceKm: String,
    var priority: String,
    var archived: String,
    var title: String,
    var statusLabel: String,
)

fun toLegacyBrokerPlanView(item: LegacyBrokerPlan): LegacyBrokerPlanView {
    return LegacyBrokerPlanView(
        code = item.code,
        name = item.name,
        maximumText = "%.2f".format(item.maximum),
        distanceKm = "%.2f".format(item.distanceKm),
        priority = item.priority.toString(),
        archived = "${item.archived}",
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyBrokerPlanViews(items: MutableList<LegacyBrokerPlan>): MutableList<LegacyBrokerPlanView> {
    val views = mutableListOf<LegacyBrokerPlanView>()
    for (item in items) {
        views.add(toLegacyBrokerPlanView(item))
    }
    return views
}

fun sumLegacyBrokerPlanView(view: LegacyBrokerPlanView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedMaximum + parsedDistanceKm + parsedPriority.toDouble()
}
