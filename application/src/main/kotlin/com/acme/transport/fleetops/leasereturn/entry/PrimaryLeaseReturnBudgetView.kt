package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary lease return budget. */
data class PrimaryLeaseReturnBudgetView(
    var codeText: String,
    var labelText: String,
    var currency: String,
    var title: String,
    var rate: String,
    var segment: String,
    var approved: String,
    var statusLabel: String,
)

fun toPrimaryLeaseReturnBudgetView(item: PrimaryLeaseReturnBudget): PrimaryLeaseReturnBudgetView {
    return PrimaryLeaseReturnBudgetView(
        codeText = item.code,
        labelText = truncate(item.label, 40),
        currency = truncate(item.currency, 80),
        title = item.title,
        rate = "%.2f".format(item.rate),
        segment = item.segment,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryLeaseReturnBudgetViews(items: MutableList<PrimaryLeaseReturnBudget>): MutableList<PrimaryLeaseReturnBudgetView> {
    val views = mutableListOf<PrimaryLeaseReturnBudgetView>()
    for (item in items) {
        views.add(toPrimaryLeaseReturnBudgetView(item))
    }
    return views
}

fun sumPrimaryLeaseReturnBudgetView(view: PrimaryLeaseReturnBudgetView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
