package com.acme.transport.lastmile.redelivery.redeliveryclaim

/** How screens and exports show a redelivery rule. */
data class RedeliveryRuleView(
    var code: String,
    var titleText: String,
    var version: String,
    var reviewer: String,
    var notes: String,
    var createdAt: String,
    var amount: String,
    var depotText: String,
    var quantity: String,
    var checksum: String,
    var statusLabel: String,
)

fun toRedeliveryRuleView(item: RedeliveryRule): RedeliveryRuleView {
    return RedeliveryRuleView(
        code = item.code,
        titleText = item.title,
        version = item.version.toString(),
        reviewer = item.reviewer,
        notes = item.notes,
        createdAt = item.createdAt.toString(),
        amount = "%.2f".format(item.amount),
        depotText = item.depot,
        quantity = item.quantity.toString(),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryRuleViews(items: MutableList<RedeliveryRule>): MutableList<RedeliveryRuleView> {
    val views = mutableListOf<RedeliveryRuleView>()
    for (item in items) {
        views.add(toRedeliveryRuleView(item))
    }
    return views
}

fun sumRedeliveryRuleView(view: RedeliveryRuleView): Double {
    val parsedVersion = view.version.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedVersion.toDouble() + parsedCreatedAt.toDouble() + parsedAmount
}
