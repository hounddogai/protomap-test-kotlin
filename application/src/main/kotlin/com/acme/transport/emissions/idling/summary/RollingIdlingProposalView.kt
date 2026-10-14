package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling idling proposal. */
data class RollingIdlingProposalView(
    var codeText: String,
    var nameText: String,
    var threshold: String,
    var odometerText: String,
    var vehicleIdText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toRollingIdlingProposalView(item: RollingIdlingProposal): RollingIdlingProposalView {
    return RollingIdlingProposalView(
        codeText = item.code,
        nameText = item.name,
        threshold = "%.2f".format(item.threshold),
        odometerText = item.odometer.toString(),
        vehicleIdText = truncate(item.vehicleId, 40),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingIdlingProposalViews(items: MutableList<RollingIdlingProposal>): MutableList<RollingIdlingProposalView> {
    val views = mutableListOf<RollingIdlingProposalView>()
    for (item in items) {
        views.add(toRollingIdlingProposalView(item))
    }
    return views
}

fun sumRollingIdlingProposalView(view: RollingIdlingProposalView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedThreshold + parsedOdometer.toDouble() + parsedAmount
}
