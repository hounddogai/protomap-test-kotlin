package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender budget. */
data class TenderBudgetView(
    var codeText: String,
    var labelText: String,
    var checksum: String,
    var nameText: String,
    var routeCode: String,
    var depot: String,
    var driverId: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toTenderBudgetView(item: TenderBudget): TenderBudgetView {
    return TenderBudgetView(
        codeText = item.code,
        labelText = item.label,
        checksum = item.checksum,
        nameText = item.name,
        routeCode = item.routeCode,
        depot = truncate(item.depot, 120),
        driverId = item.driverId,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderBudgetViews(items: MutableList<TenderBudget>): MutableList<TenderBudgetView> {
    val views = mutableListOf<TenderBudgetView>()
    for (item in items) {
        views.add(toTenderBudgetView(item))
    }
    return views
}

fun sumTenderBudgetView(view: TenderBudgetView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}
