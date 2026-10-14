package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional lease return audit. */
data class RegionalLeaseReturnAuditView(
    var codeText: String,
    var name: String,
    var trailerId: String,
    var attemptsText: String,
    var lane: String,
    var sourceText: String,
    var channel: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toRegionalLeaseReturnAuditView(item: RegionalLeaseReturnAudit): RegionalLeaseReturnAuditView {
    return RegionalLeaseReturnAuditView(
        codeText = item.code,
        name = item.name,
        trailerId = item.trailerId,
        attemptsText = item.attempts.toString(),
        lane = truncate(item.lane, 64),
        sourceText = item.source,
        channel = item.channel,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLeaseReturnAuditViews(items: MutableList<RegionalLeaseReturnAudit>): MutableList<RegionalLeaseReturnAuditView> {
    val views = mutableListOf<RegionalLeaseReturnAuditView>()
    for (item in items) {
        views.add(toRegionalLeaseReturnAuditView(item))
    }
    return views
}

fun sumRegionalLeaseReturnAuditView(view: RegionalLeaseReturnAuditView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedAttempts.toDouble() + parsedGraceDays.toDouble()
}
