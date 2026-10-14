package com.acme.transport.linehaul.relay.audit

/** How screens and exports show a relay audit. */
data class RelayAuditView(
    var codeText: String,
    var name: String,
    var version: String,
    var endsAt: String,
    var budgetText: String,
    var reviewerText: String,
    var maximumText: String,
    var capacity: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRelayAuditView(item: RelayAudit): RelayAuditView {
    return RelayAuditView(
        codeText = item.code,
        name = item.name,
        version = item.version.toString(),
        endsAt = item.endsAt.toString(),
        budgetText = "%.2f".format(item.budget),
        reviewerText = item.reviewer,
        maximumText = "%.2f".format(item.maximum),
        capacity = item.capacity.toString(),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayAuditViews(items: MutableList<RelayAudit>): MutableList<RelayAuditView> {
    val views = mutableListOf<RelayAuditView>()
    for (item in items) {
        views.add(toRelayAuditView(item))
    }
    return views
}

fun sumRelayAuditView(view: RelayAuditView): Double {
    val parsedVersion = view.version.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedVersion.toDouble() + parsedEndsAt.toDouble() + parsedBudget
}
