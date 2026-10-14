package com.acme.transport.crewing.swap.claim

/** How screens and exports show a swap allocation. */
data class SwapAllocationView(
    var code: String,
    var title: String,
    var shiftCode: String,
    var costText: String,
    var priorityText: String,
    var marginText: String,
    var statusLabel: String,
)

fun toSwapAllocationView(item: SwapAllocation): SwapAllocationView {
    return SwapAllocationView(
        code = item.code,
        title = item.title,
        shiftCode = item.shiftCode,
        costText = "%.2f".format(item.cost),
        priorityText = item.priority.toString(),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapAllocationViews(items: MutableList<SwapAllocation>): MutableList<SwapAllocationView> {
    val views = mutableListOf<SwapAllocationView>()
    for (item in items) {
        views.add(toSwapAllocationView(item))
    }
    return views
}

fun sumSwapAllocationView(view: SwapAllocationView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedCost + parsedPriority.toDouble() + parsedMargin
}
