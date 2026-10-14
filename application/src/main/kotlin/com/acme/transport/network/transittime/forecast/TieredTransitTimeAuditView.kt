package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered transit time audit. */
data class TieredTransitTimeAuditView(
    var codeText: String,
    var title: String,
    var unit: String,
    var segment: String,
    var budgetText: String,
    var summary: String,
    var statusLabel: String,
)

fun toTieredTransitTimeAuditView(item: TieredTransitTimeAudit): TieredTransitTimeAuditView {
    return TieredTransitTimeAuditView(
        codeText = item.code,
        title = item.title,
        unit = item.unit,
        segment = item.segment,
        budgetText = "%.2f".format(item.budget),
        summary = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTransitTimeAuditViews(items: MutableList<TieredTransitTimeAudit>): MutableList<TieredTransitTimeAuditView> {
    val views = mutableListOf<TieredTransitTimeAuditView>()
    for (item in items) {
        views.add(toTieredTransitTimeAuditView(item))
    }
    return views
}

fun sumTieredTransitTimeAuditView(view: TieredTransitTimeAuditView): Double {
    val parsedBudget = view.budgetText.toDouble()
    return parsedBudget
}
