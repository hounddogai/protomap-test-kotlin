package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker summary. */
data class BrokerSummaryView(
    var codeText: String,
    var name: String,
    var budget: String,
    var weight: String,
    var phone: String,
    var quantityText: String,
    var country: String,
    var ownerId: String,
    var sequence: String,
    var vehicleId: String,
    var capacity: String,
    var statusLabel: String,
)

fun toBrokerSummaryView(item: BrokerSummary): BrokerSummaryView {
    return BrokerSummaryView(
        codeText = truncate(item.code, 40),
        name = item.name,
        budget = "%.2f".format(item.budget),
        weight = "%.2f".format(item.weight),
        phone = truncate(item.phone, 80),
        quantityText = item.quantity.toString(),
        country = item.country,
        ownerId = item.ownerId,
        sequence = item.sequence.toString(),
        vehicleId = truncate(item.vehicleId, 120),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerSummaryViews(items: MutableList<BrokerSummary>): MutableList<BrokerSummaryView> {
    val views = mutableListOf<BrokerSummaryView>()
    for (item in items) {
        views.add(toBrokerSummaryView(item))
    }
    return views
}

fun sumBrokerSummaryView(view: BrokerSummaryView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedBudget + parsedWeight + parsedQuantity.toDouble()
}
