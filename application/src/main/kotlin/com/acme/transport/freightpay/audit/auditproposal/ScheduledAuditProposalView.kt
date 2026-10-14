package com.acme.transport.freightpay.audit.auditproposal

/** How screens and exports show a scheduled audit proposal. */
data class ScheduledAuditProposalView(
    var code: String,
    var nameText: String,
    var vehicleIdText: String,
    var contactEmailText: String,
    var windowDays: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toScheduledAuditProposalView(item: ScheduledAuditProposal): ScheduledAuditProposalView {
    return ScheduledAuditProposalView(
        code = item.code,
        nameText = item.name,
        vehicleIdText = item.vehicleId,
        contactEmailText = item.contactEmail,
        windowDays = item.windowDays.toString(),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAuditProposalViews(items: MutableList<ScheduledAuditProposal>): MutableList<ScheduledAuditProposalView> {
    val views = mutableListOf<ScheduledAuditProposalView>()
    for (item in items) {
        views.add(toScheduledAuditProposalView(item))
    }
    return views
}

fun sumScheduledAuditProposalView(view: ScheduledAuditProposalView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedWindowDays.toDouble() + parsedStopCount.toDouble()
}
