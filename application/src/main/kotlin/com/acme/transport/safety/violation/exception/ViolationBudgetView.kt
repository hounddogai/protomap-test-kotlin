package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation budget. */
data class ViolationBudgetView(
    var code: String,
    var name: String,
    var minimum: String,
    var quantity: String,
    var limit: String,
    var summary: String,
    var shiftCode: String,
    var checksum: String,
    var statusLabel: String,
)

fun toViolationBudgetView(item: ViolationBudget): ViolationBudgetView {
    return ViolationBudgetView(
        code = item.code,
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        quantity = item.quantity.toString(),
        limit = item.limit.toString(),
        summary = truncate(item.summary, 40),
        shiftCode = item.shiftCode,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationBudgetViews(items: MutableList<ViolationBudget>): MutableList<ViolationBudgetView> {
    val views = mutableListOf<ViolationBudgetView>()
    for (item in items) {
        views.add(toViolationBudgetView(item))
    }
    return views
}

fun sumViolationBudgetView(view: ViolationBudgetView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedMinimum + parsedQuantity.toDouble() + parsedLimit.toDouble()
}
