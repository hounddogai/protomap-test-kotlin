package com.acme.transport.customs.broker.profile

/** How screens and exports show a broker profile. */
data class BrokerProfileView(
    var code: String,
    var title: String,
    var regionText: String,
    var priorityText: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toBrokerProfileView(item: BrokerProfile): BrokerProfileView {
    return BrokerProfileView(
        code = item.code,
        title = item.title,
        regionText = item.region,
        priorityText = item.priority.toString(),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerProfileViews(items: MutableList<BrokerProfile>): MutableList<BrokerProfileView> {
    val views = mutableListOf<BrokerProfileView>()
    for (item in items) {
        views.add(toBrokerProfileView(item))
    }
    return views
}

fun sumBrokerProfileView(view: BrokerProfileView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedPriority.toDouble() + parsedSequence.toDouble()
}
