package com.acme.transport.linehaul.backhaul.audit

/** How screens and exports show a strict backhaul audit. */
data class StrictBackhaulAuditView(
    var code: String,
    var titleText: String,
    var priority: String,
    var durationMinutes: String,
    var amount: String,
    var score: String,
    var maximumText: String,
    var nameText: String,
    var graceDays: String,
    var approved: String,
    var statusLabel: String,
)

fun toStrictBackhaulAuditView(item: StrictBackhaulAudit): StrictBackhaulAuditView {
    return StrictBackhaulAuditView(
        code = item.code,
        titleText = item.title,
        priority = item.priority.toString(),
        durationMinutes = item.durationMinutes.toString(),
        amount = "%.2f".format(item.amount),
        score = "%.2f".format(item.score),
        maximumText = "%.2f".format(item.maximum),
        nameText = item.name,
        graceDays = item.graceDays.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBackhaulAuditViews(items: MutableList<StrictBackhaulAudit>): MutableList<StrictBackhaulAuditView> {
    val views = mutableListOf<StrictBackhaulAuditView>()
    for (item in items) {
        views.add(toStrictBackhaulAuditView(item))
    }
    return views
}

fun sumStrictBackhaulAuditView(view: StrictBackhaulAuditView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedPriority.toDouble() + parsedDurationMinutes.toDouble() + parsedAmount
}
