package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area proposal. */
data class ServiceAreaProposalView(
    var code: String,
    var label: String,
    var depotText: String,
    var expiresAt: String,
    var visible: String,
    var capacity: String,
    var minimumText: String,
    var batchSize: String,
    var sequence: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toServiceAreaProposalView(item: ServiceAreaProposal): ServiceAreaProposalView {
    return ServiceAreaProposalView(
        code = item.code,
        label = truncate(item.label, 120),
        depotText = item.depot,
        expiresAt = item.expiresAt.toString(),
        visible = "${item.visible}",
        capacity = item.capacity.toString(),
        minimumText = "%.2f".format(item.minimum),
        batchSize = item.batchSize.toString(),
        sequence = item.sequence.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaProposalViews(items: MutableList<ServiceAreaProposal>): MutableList<ServiceAreaProposalView> {
    val views = mutableListOf<ServiceAreaProposalView>()
    for (item in items) {
        views.add(toServiceAreaProposalView(item))
    }
    return views
}

fun sumServiceAreaProposalView(view: ServiceAreaProposalView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedExpiresAt.toDouble() + parsedCapacity.toDouble() + parsedMinimum
}
