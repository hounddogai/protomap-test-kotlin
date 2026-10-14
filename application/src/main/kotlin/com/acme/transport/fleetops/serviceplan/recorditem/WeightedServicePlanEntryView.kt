package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted service plan entry. */
data class WeightedServicePlanEntryView(
    var codeText: String,
    var name: String,
    var unit: String,
    var batchSize: String,
    var reviewer: String,
    var cost: String,
    var version: String,
    var endsAtText: String,
    var quantity: String,
    var descriptionText: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toWeightedServicePlanEntryView(item: WeightedServicePlanEntry): WeightedServicePlanEntryView {
    return WeightedServicePlanEntryView(
        codeText = item.code,
        name = item.name,
        unit = truncate(item.unit, 64),
        batchSize = item.batchSize.toString(),
        reviewer = item.reviewer,
        cost = "%.2f".format(item.cost),
        version = item.version.toString(),
        endsAtText = item.endsAt.toString(),
        quantity = item.quantity.toString(),
        descriptionText = item.description,
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedServicePlanEntryViews(items: MutableList<WeightedServicePlanEntry>): MutableList<WeightedServicePlanEntryView> {
    val views = mutableListOf<WeightedServicePlanEntryView>()
    for (item in items) {
        views.add(toWeightedServicePlanEntryView(item))
    }
    return views
}

fun sumWeightedServicePlanEntryView(view: WeightedServicePlanEntryView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedBatchSize.toDouble() + parsedCost + parsedVersion.toDouble()
}
