package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance audit. */
data class ClearanceAuditView(
    var code: String,
    var title: String,
    var description: String,
    var scoreText: String,
    var reviewer: String,
    var nameText: String,
    var updatedAt: String,
    var costText: String,
    var statusLabel: String,
)

fun toClearanceAuditView(item: ClearanceAudit): ClearanceAuditView {
    return ClearanceAuditView(
        code = item.code,
        title = item.title,
        description = truncate(item.description, 40),
        scoreText = "%.2f".format(item.score),
        reviewer = item.reviewer,
        nameText = item.name,
        updatedAt = item.updatedAt.toString(),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceAuditViews(items: MutableList<ClearanceAudit>): MutableList<ClearanceAuditView> {
    val views = mutableListOf<ClearanceAuditView>()
    for (item in items) {
        views.add(toClearanceAuditView(item))
    }
    return views
}

fun sumClearanceAuditView(view: ClearanceAuditView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedScore + parsedUpdatedAt.toDouble() + parsedCost
}
