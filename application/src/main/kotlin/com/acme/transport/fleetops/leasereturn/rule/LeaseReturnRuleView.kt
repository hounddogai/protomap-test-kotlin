package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return rule. */
data class LeaseReturnRuleView(
    var code: String,
    var nameText: String,
    var segment: String,
    var loadKg: String,
    var ceiling: String,
    var createdAtText: String,
    var palletsText: String,
    var budget: String,
    var expiresAt: String,
    var unit: String,
    var statusLabel: String,
)

fun toLeaseReturnRuleView(item: LeaseReturnRule): LeaseReturnRuleView {
    return LeaseReturnRuleView(
        code = item.code,
        nameText = item.name,
        segment = truncate(item.segment, 40),
        loadKg = "%.2f".format(item.loadKg),
        ceiling = "%.2f".format(item.ceiling),
        createdAtText = item.createdAt.toString(),
        palletsText = item.pallets.toString(),
        budget = "%.2f".format(item.budget),
        expiresAt = item.expiresAt.toString(),
        unit = truncate(item.unit, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnRuleViews(items: MutableList<LeaseReturnRule>): MutableList<LeaseReturnRuleView> {
    val views = mutableListOf<LeaseReturnRuleView>()
    for (item in items) {
        views.add(toLeaseReturnRuleView(item))
    }
    return views
}

fun sumLeaseReturnRuleView(view: LeaseReturnRuleView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedLoadKg + parsedCeiling + parsedCreatedAt.toDouble()
}
