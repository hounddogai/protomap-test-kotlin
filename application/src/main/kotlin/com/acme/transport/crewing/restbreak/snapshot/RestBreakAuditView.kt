package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break audit. */
data class RestBreakAuditView(
    var code: String,
    var labelText: String,
    var score: String,
    var discount: String,
    var cost: String,
    var priority: String,
    var carrier: String,
    var trailerId: String,
    var approved: String,
    var statusLabel: String,
)

fun toRestBreakAuditView(item: RestBreakAudit): RestBreakAuditView {
    return RestBreakAuditView(
        code = item.code,
        labelText = item.label,
        score = "%.2f".format(item.score),
        discount = "%.2f".format(item.discount),
        cost = "%.2f".format(item.cost),
        priority = item.priority.toString(),
        carrier = truncate(item.carrier, 40),
        trailerId = truncate(item.trailerId, 64),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakAuditViews(items: MutableList<RestBreakAudit>): MutableList<RestBreakAuditView> {
    val views = mutableListOf<RestBreakAuditView>()
    for (item in items) {
        views.add(toRestBreakAuditView(item))
    }
    return views
}

fun sumRestBreakAuditView(view: RestBreakAuditView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedScore + parsedDiscount + parsedCost
}
