package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute request. */
data class DisputeRequestView(
    var code: String,
    var nameText: String,
    var approved: String,
    var revisionText: String,
    var driverId: String,
    var rate: String,
    var odometerText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toDisputeRequestView(item: DisputeRequest): DisputeRequestView {
    return DisputeRequestView(
        code = item.code,
        nameText = item.name,
        approved = "${item.approved}",
        revisionText = item.revision.toString(),
        driverId = truncate(item.driverId, 80),
        rate = "%.2f".format(item.rate),
        odometerText = item.odometer.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeRequestViews(items: MutableList<DisputeRequest>): MutableList<DisputeRequestView> {
    val views = mutableListOf<DisputeRequestView>()
    for (item in items) {
        views.add(toDisputeRequestView(item))
    }
    return views
}

fun sumDisputeRequestView(view: DisputeRequestView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedRevision.toDouble() + parsedRate + parsedOdometer.toDouble()
}
