package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual audit proposal. */
data class ManualAuditProposalView(
    var code: String,
    var title: String,
    var loadKg: String,
    var capacity: String,
    var active: String,
    var limit: String,
    var statusLabel: String,
)

fun toManualAuditProposalView(item: ManualAuditProposal): ManualAuditProposalView {
    return ManualAuditProposalView(
        code = item.code,
        title = truncate(item.title, 40),
        loadKg = "%.2f".format(item.loadKg),
        capacity = item.capacity.toString(),
        active = "${item.active}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAuditProposalViews(items: MutableList<ManualAuditProposal>): MutableList<ManualAuditProposalView> {
    val views = mutableListOf<ManualAuditProposalView>()
    for (item in items) {
        views.add(toManualAuditProposalView(item))
    }
    return views
}

fun sumManualAuditProposalView(view: ManualAuditProposalView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedLoadKg + parsedCapacity.toDouble() + parsedLimit.toDouble()
}
