package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit window. */
data class AuditWindowView(
    var codeText: String,
    var title: String,
    var budget: String,
    var pinnedText: String,
    var startsAtText: String,
    var batchSize: String,
    var score: String,
    var pallets: String,
    var statusLabel: String,
)

fun toAuditWindowView(item: AuditWindow): AuditWindowView {
    return AuditWindowView(
        codeText = item.code,
        title = truncate(item.title, 120),
        budget = "%.2f".format(item.budget),
        pinnedText = "${item.pinned}",
        startsAtText = item.startsAt.toString(),
        batchSize = item.batchSize.toString(),
        score = "%.2f".format(item.score),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditWindowViews(items: MutableList<AuditWindow>): MutableList<AuditWindowView> {
    val views = mutableListOf<AuditWindowView>()
    for (item in items) {
        views.add(toAuditWindowView(item))
    }
    return views
}

fun sumAuditWindowView(view: AuditWindowView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBudget + parsedStartsAt.toDouble() + parsedBatchSize.toDouble()
}
