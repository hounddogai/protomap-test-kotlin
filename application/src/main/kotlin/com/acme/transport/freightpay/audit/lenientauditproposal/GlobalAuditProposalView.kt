package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global audit proposal. */
data class GlobalAuditProposalView(
    var code: String,
    var title: String,
    var locale: String,
    var region: String,
    var unitText: String,
    var floorValue: String,
    var sourceText: String,
    var summary: String,
    var createdAt: String,
    var attemptsText: String,
    var revision: String,
    var statusLabel: String,
)

fun toGlobalAuditProposalView(item: GlobalAuditProposal): GlobalAuditProposalView {
    return GlobalAuditProposalView(
        code = item.code,
        title = truncate(item.title, 40),
        locale = item.locale,
        region = truncate(item.region, 40),
        unitText = truncate(item.unit, 80),
        floorValue = "%.2f".format(item.floorValue),
        sourceText = truncate(item.source, 64),
        summary = item.summary,
        createdAt = item.createdAt.toString(),
        attemptsText = item.attempts.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAuditProposalViews(items: MutableList<GlobalAuditProposal>): MutableList<GlobalAuditProposalView> {
    val views = mutableListOf<GlobalAuditProposalView>()
    for (item in items) {
        views.add(toGlobalAuditProposalView(item))
    }
    return views
}

fun sumGlobalAuditProposalView(view: GlobalAuditProposalView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedFloorValue + parsedCreatedAt.toDouble() + parsedAttempts.toDouble()
}
