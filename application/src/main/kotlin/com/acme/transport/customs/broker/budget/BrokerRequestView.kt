package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker request. */
data class BrokerRequestView(
    var code: String,
    var labelText: String,
    var odometer: String,
    var revisionText: String,
    var pinnedText: String,
    var score: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toBrokerRequestView(item: BrokerRequest): BrokerRequestView {
    return BrokerRequestView(
        code = truncate(item.code, 120),
        labelText = truncate(item.label, 64),
        odometer = item.odometer.toString(),
        revisionText = item.revision.toString(),
        pinnedText = "${item.pinned}",
        score = "%.2f".format(item.score),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerRequestViews(items: MutableList<BrokerRequest>): MutableList<BrokerRequestView> {
    val views = mutableListOf<BrokerRequestView>()
    for (item in items) {
        views.add(toBrokerRequestView(item))
    }
    return views
}

fun sumBrokerRequestView(view: BrokerRequestView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedOdometer.toDouble() + parsedRevision.toDouble() + parsedScore
}
