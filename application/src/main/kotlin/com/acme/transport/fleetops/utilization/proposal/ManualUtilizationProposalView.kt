package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual utilization proposal. */
data class ManualUtilizationProposalView(
    var code: String,
    var name: String,
    var minimum: String,
    var sequence: String,
    var quantity: String,
    var source: String,
    var phone: String,
    var budget: String,
    var createdAtText: String,
    var vehicleId: String,
    var amount: String,
    var statusLabel: String,
)

fun toManualUtilizationProposalView(item: ManualUtilizationProposal): ManualUtilizationProposalView {
    return ManualUtilizationProposalView(
        code = item.code,
        name = truncate(item.name, 120),
        minimum = "%.2f".format(item.minimum),
        sequence = item.sequence.toString(),
        quantity = item.quantity.toString(),
        source = truncate(item.source, 80),
        phone = item.phone,
        budget = "%.2f".format(item.budget),
        createdAtText = item.createdAt.toString(),
        vehicleId = item.vehicleId,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualUtilizationProposalViews(items: MutableList<ManualUtilizationProposal>): MutableList<ManualUtilizationProposalView> {
    val views = mutableListOf<ManualUtilizationProposalView>()
    for (item in items) {
        views.add(toManualUtilizationProposalView(item))
    }
    return views
}

fun sumManualUtilizationProposalView(view: ManualUtilizationProposalView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedMinimum + parsedSequence.toDouble() + parsedQuantity.toDouble()
}
