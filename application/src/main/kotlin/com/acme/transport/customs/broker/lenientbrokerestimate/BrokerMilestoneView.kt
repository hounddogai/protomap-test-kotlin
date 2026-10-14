package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker milestone. */
data class BrokerMilestoneView(
    var codeText: String,
    var nameText: String,
    var ownerId: String,
    var ceiling: String,
    var notes: String,
    var labelText: String,
    var weight: String,
    var segment: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toBrokerMilestoneView(item: BrokerMilestone): BrokerMilestoneView {
    return BrokerMilestoneView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        ownerId = truncate(item.ownerId, 120),
        ceiling = "%.2f".format(item.ceiling),
        notes = item.notes,
        labelText = item.label,
        weight = "%.2f".format(item.weight),
        segment = item.segment,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerMilestoneViews(items: MutableList<BrokerMilestone>): MutableList<BrokerMilestoneView> {
    val views = mutableListOf<BrokerMilestoneView>()
    for (item in items) {
        views.add(toBrokerMilestoneView(item))
    }
    return views
}

fun sumBrokerMilestoneView(view: BrokerMilestoneView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCeiling + parsedWeight + parsedGraceDays.toDouble()
}
