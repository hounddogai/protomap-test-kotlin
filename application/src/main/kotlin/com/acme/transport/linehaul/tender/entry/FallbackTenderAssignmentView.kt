package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback tender assignment. */
data class FallbackTenderAssignmentView(
    var code: String,
    var label: String,
    var limitText: String,
    var cost: String,
    var rate: String,
    var statusLabel: String,
)

fun toFallbackTenderAssignmentView(item: FallbackTenderAssignment): FallbackTenderAssignmentView {
    return FallbackTenderAssignmentView(
        code = item.code,
        label = truncate(item.label, 40),
        limitText = item.limit.toString(),
        cost = "%.2f".format(item.cost),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTenderAssignmentViews(items: MutableList<FallbackTenderAssignment>): MutableList<FallbackTenderAssignmentView> {
    val views = mutableListOf<FallbackTenderAssignmentView>()
    for (item in items) {
        views.add(toFallbackTenderAssignmentView(item))
    }
    return views
}

fun sumFallbackTenderAssignmentView(view: FallbackTenderAssignmentView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedLimit.toDouble() + parsedCost + parsedRate
}
