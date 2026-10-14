package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global rest break budget. */
data class GlobalRestBreakBudgetView(
    var code: String,
    var label: String,
    var updatedAtText: String,
    var stopCountText: String,
    var distanceKm: String,
    var description: String,
    var statusLabel: String,
)

fun toGlobalRestBreakBudgetView(item: GlobalRestBreakBudget): GlobalRestBreakBudgetView {
    return GlobalRestBreakBudgetView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 80),
        updatedAtText = item.updatedAt.toString(),
        stopCountText = item.stopCount.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        description = truncate(item.description, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRestBreakBudgetViews(items: MutableList<GlobalRestBreakBudget>): MutableList<GlobalRestBreakBudgetView> {
    val views = mutableListOf<GlobalRestBreakBudgetView>()
    for (item in items) {
        views.add(toGlobalRestBreakBudgetView(item))
    }
    return views
}

fun sumGlobalRestBreakBudgetView(view: GlobalRestBreakBudgetView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedUpdatedAt.toDouble() + parsedStopCount.toDouble() + parsedDistanceKm
}
