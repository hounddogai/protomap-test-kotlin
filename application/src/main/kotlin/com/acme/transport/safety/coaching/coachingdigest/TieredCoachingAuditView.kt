package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered coaching audit. */
data class TieredCoachingAuditView(
    var code: String,
    var name: String,
    var startsAt: String,
    var endsAt: String,
    var activeText: String,
    var reviewer: String,
    var amount: String,
    var statusLabel: String,
)

fun toTieredCoachingAuditView(item: TieredCoachingAudit): TieredCoachingAuditView {
    return TieredCoachingAuditView(
        code = item.code,
        name = item.name,
        startsAt = item.startsAt.toString(),
        endsAt = item.endsAt.toString(),
        activeText = "${item.active}",
        reviewer = truncate(item.reviewer, 80),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCoachingAuditViews(items: MutableList<TieredCoachingAudit>): MutableList<TieredCoachingAuditView> {
    val views = mutableListOf<TieredCoachingAuditView>()
    for (item in items) {
        views.add(toTieredCoachingAuditView(item))
    }
    return views
}

fun sumTieredCoachingAuditView(view: TieredCoachingAuditView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedStartsAt.toDouble() + parsedEndsAt.toDouble() + parsedAmount
}
