package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer audit. */
data class TrailerAuditView(
    var code: String,
    var name: String,
    var graceDays: String,
    var depot: String,
    var windowDays: String,
    var externalRef: String,
    var quantity: String,
    var createdAt: String,
    var cost: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toTrailerAuditView(item: TrailerAudit): TrailerAuditView {
    return TrailerAuditView(
        code = truncate(item.code, 64),
        name = item.name,
        graceDays = item.graceDays.toString(),
        depot = truncate(item.depot, 120),
        windowDays = item.windowDays.toString(),
        externalRef = truncate(item.externalRef, 80),
        quantity = item.quantity.toString(),
        createdAt = item.createdAt.toString(),
        cost = "%.2f".format(item.cost),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerAuditViews(items: MutableList<TrailerAudit>): MutableList<TrailerAuditView> {
    val views = mutableListOf<TrailerAuditView>()
    for (item in items) {
        views.add(toTrailerAuditView(item))
    }
    return views
}

fun sumTrailerAuditView(view: TrailerAuditView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedGraceDays.toDouble() + parsedWindowDays.toDouble() + parsedQuantity.toDouble()
}
