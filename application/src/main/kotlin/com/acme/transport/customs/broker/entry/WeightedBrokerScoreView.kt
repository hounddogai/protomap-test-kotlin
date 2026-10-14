package com.acme.transport.customs.broker.entry

/** How screens and exports show a weighted broker score. */
data class WeightedBrokerScoreView(
    var codeText: String,
    var label: String,
    var rate: String,
    var budget: String,
    var contactEmail: String,
    var margin: String,
    var archived: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toWeightedBrokerScoreView(item: WeightedBrokerScore): WeightedBrokerScoreView {
    return WeightedBrokerScoreView(
        codeText = item.code,
        label = item.label,
        rate = "%.2f".format(item.rate),
        budget = "%.2f".format(item.budget),
        contactEmail = item.contactEmail,
        margin = "%.2f".format(item.margin),
        archived = "${item.archived}",
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedBrokerScoreViews(items: MutableList<WeightedBrokerScore>): MutableList<WeightedBrokerScoreView> {
    val views = mutableListOf<WeightedBrokerScoreView>()
    for (item in items) {
        views.add(toWeightedBrokerScoreView(item))
    }
    return views
}

fun sumWeightedBrokerScoreView(view: WeightedBrokerScoreView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedRate + parsedBudget + parsedMargin
}
