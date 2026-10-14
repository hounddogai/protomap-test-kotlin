package com.acme.transport.customs.broker.budget

/** How screens and exports show a broker slot. */
data class BrokerSlotView(
    var codeText: String,
    var name: String,
    var laneText: String,
    var approved: String,
    var createdAtText: String,
    var segment: String,
    var visibleText: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toBrokerSlotView(item: BrokerSlot): BrokerSlotView {
    return BrokerSlotView(
        codeText = item.code,
        name = item.name,
        laneText = item.lane,
        approved = "${item.approved}",
        createdAtText = item.createdAt.toString(),
        segment = item.segment,
        visibleText = "${item.visible}",
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerSlotViews(items: MutableList<BrokerSlot>): MutableList<BrokerSlotView> {
    val views = mutableListOf<BrokerSlotView>()
    for (item in items) {
        views.add(toBrokerSlotView(item))
    }
    return views
}

fun sumBrokerSlotView(view: BrokerSlotView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedCreatedAt.toDouble() + parsedEtaMinutes.toDouble()
}
