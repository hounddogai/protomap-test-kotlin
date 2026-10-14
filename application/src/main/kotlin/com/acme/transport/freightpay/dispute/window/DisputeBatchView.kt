package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute batch. */
data class DisputeBatchView(
    var code: String,
    var titleText: String,
    var attemptsText: String,
    var routeCode: String,
    var ceiling: String,
    var labelText: String,
    var cost: String,
    var approved: String,
    var statusLabel: String,
)

fun toDisputeBatchView(item: DisputeBatch): DisputeBatchView {
    return DisputeBatchView(
        code = truncate(item.code, 80),
        titleText = item.title,
        attemptsText = item.attempts.toString(),
        routeCode = truncate(item.routeCode, 40),
        ceiling = "%.2f".format(item.ceiling),
        labelText = truncate(item.label, 80),
        cost = "%.2f".format(item.cost),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeBatchViews(items: MutableList<DisputeBatch>): MutableList<DisputeBatchView> {
    val views = mutableListOf<DisputeBatchView>()
    for (item in items) {
        views.add(toDisputeBatchView(item))
    }
    return views
}

fun sumDisputeBatchView(view: DisputeBatchView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedAttempts.toDouble() + parsedCeiling + parsedCost
}
