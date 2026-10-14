package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched duty period proposal. */
data class BatchedDutyPeriodProposalView(
    var code: String,
    var labelText: String,
    var channel: String,
    var shiftCode: String,
    var graceDays: String,
    var pinned: String,
    var vehicleId: String,
    var odometer: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toBatchedDutyPeriodProposalView(item: BatchedDutyPeriodProposal): BatchedDutyPeriodProposalView {
    return BatchedDutyPeriodProposalView(
        code = truncate(item.code, 80),
        labelText = item.label,
        channel = item.channel,
        shiftCode = item.shiftCode,
        graceDays = item.graceDays.toString(),
        pinned = "${item.pinned}",
        vehicleId = truncate(item.vehicleId, 80),
        odometer = item.odometer.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDutyPeriodProposalViews(items: MutableList<BatchedDutyPeriodProposal>): MutableList<BatchedDutyPeriodProposalView> {
    val views = mutableListOf<BatchedDutyPeriodProposalView>()
    for (item in items) {
        views.add(toBatchedDutyPeriodProposalView(item))
    }
    return views
}

fun sumBatchedDutyPeriodProposalView(view: BatchedDutyPeriodProposalView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedGraceDays.toDouble() + parsedOdometer.toDouble()
}
