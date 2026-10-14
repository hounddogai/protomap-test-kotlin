package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker policy. */
data class BrokerPolicyView(
    var codeText: String,
    var title: String,
    var carrier: String,
    var priority: String,
    var stopCount: String,
    var region: String,
    var revision: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toBrokerPolicyView(item: BrokerPolicy): BrokerPolicyView {
    return BrokerPolicyView(
        codeText = item.code,
        title = truncate(item.title, 40),
        carrier = truncate(item.carrier, 40),
        priority = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        region = item.region,
        revision = item.revision.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerPolicyViews(items: MutableList<BrokerPolicy>): MutableList<BrokerPolicyView> {
    val views = mutableListOf<BrokerPolicyView>()
    for (item in items) {
        views.add(toBrokerPolicyView(item))
    }
    return views
}

fun sumBrokerPolicyView(view: BrokerPolicyView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedPriority.toDouble() + parsedStopCount.toDouble() + parsedRevision.toDouble()
}
