package com.acme.transport.customs.clearance.overrideitem

/** How screens and exports show a clearance allocation. */
data class ClearanceAllocationView(
    var codeText: String,
    var label: String,
    var limit: String,
    var currency: String,
    var externalRef: String,
    var stopCountText: String,
    var margin: String,
    var expiresAt: String,
    var cost: String,
    var name: String,
    var statusLabel: String,
)

fun toClearanceAllocationView(item: ClearanceAllocation): ClearanceAllocationView {
    return ClearanceAllocationView(
        codeText = item.code,
        label = item.label,
        limit = item.limit.toString(),
        currency = item.currency,
        externalRef = item.externalRef,
        stopCountText = item.stopCount.toString(),
        margin = "%.2f".format(item.margin),
        expiresAt = item.expiresAt.toString(),
        cost = "%.2f".format(item.cost),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceAllocationViews(items: MutableList<ClearanceAllocation>): MutableList<ClearanceAllocationView> {
    val views = mutableListOf<ClearanceAllocationView>()
    for (item in items) {
        views.add(toClearanceAllocationView(item))
    }
    return views
}

fun sumClearanceAllocationView(view: ClearanceAllocationView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedLimit.toDouble() + parsedStopCount.toDouble() + parsedMargin
}
