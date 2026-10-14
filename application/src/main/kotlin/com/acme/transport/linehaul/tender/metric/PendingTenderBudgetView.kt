package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending tender budget. */
data class PendingTenderBudgetView(
    var code: String,
    var titleText: String,
    var updatedAt: String,
    var shiftCodeText: String,
    var activeText: String,
    var durationMinutes: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toPendingTenderBudgetView(item: PendingTenderBudget): PendingTenderBudgetView {
    return PendingTenderBudgetView(
        code = item.code,
        titleText = truncate(item.title, 80),
        updatedAt = item.updatedAt.toString(),
        shiftCodeText = truncate(item.shiftCode, 80),
        activeText = "${item.active}",
        durationMinutes = item.durationMinutes.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTenderBudgetViews(items: MutableList<PendingTenderBudget>): MutableList<PendingTenderBudgetView> {
    val views = mutableListOf<PendingTenderBudgetView>()
    for (item in items) {
        views.add(toPendingTenderBudgetView(item))
    }
    return views
}

fun sumPendingTenderBudgetView(view: PendingTenderBudgetView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedUpdatedAt.toDouble() + parsedDurationMinutes.toDouble() + parsedFloorValue
}
