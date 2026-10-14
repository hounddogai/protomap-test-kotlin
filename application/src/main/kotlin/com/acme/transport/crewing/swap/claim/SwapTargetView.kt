package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap target. */
data class SwapTargetView(
    var codeText: String,
    var name: String,
    var reviewer: String,
    var budgetText: String,
    var updatedAt: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toSwapTargetView(item: SwapTarget): SwapTargetView {
    return SwapTargetView(
        codeText = truncate(item.code, 120),
        name = item.name,
        reviewer = item.reviewer,
        budgetText = "%.2f".format(item.budget),
        updatedAt = item.updatedAt.toString(),
        trailerIdText = truncate(item.trailerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapTargetViews(items: MutableList<SwapTarget>): MutableList<SwapTargetView> {
    val views = mutableListOf<SwapTargetView>()
    for (item in items) {
        views.add(toSwapTargetView(item))
    }
    return views
}

fun sumSwapTargetView(view: SwapTargetView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedBudget + parsedUpdatedAt.toDouble()
}
