package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap rule. */
data class SwapRuleView(
    var code: String,
    var titleText: String,
    var minimum: String,
    var depot: String,
    var priority: String,
    var archived: String,
    var stopCountText: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toSwapRuleView(item: SwapRule): SwapRuleView {
    return SwapRuleView(
        code = truncate(item.code, 120),
        titleText = truncate(item.title, 64),
        minimum = "%.2f".format(item.minimum),
        depot = item.depot,
        priority = item.priority.toString(),
        archived = "${item.archived}",
        stopCountText = item.stopCount.toString(),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapRuleViews(items: MutableList<SwapRule>): MutableList<SwapRuleView> {
    val views = mutableListOf<SwapRuleView>()
    for (item in items) {
        views.add(toSwapRuleView(item))
    }
    return views
}

fun sumSwapRuleView(view: SwapRuleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedMinimum + parsedPriority.toDouble() + parsedStopCount.toDouble()
}
