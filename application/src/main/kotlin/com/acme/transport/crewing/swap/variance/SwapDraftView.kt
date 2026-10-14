package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap draft. */
data class SwapDraftView(
    var codeText: String,
    var name: String,
    var retries: String,
    var floorValue: String,
    var driverId: String,
    var costText: String,
    var weight: String,
    var externalRefText: String,
    var durationMinutes: String,
    var thresholdText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toSwapDraftView(item: SwapDraft): SwapDraftView {
    return SwapDraftView(
        codeText = item.code,
        name = item.name,
        retries = item.retries.toString(),
        floorValue = "%.2f".format(item.floorValue),
        driverId = truncate(item.driverId, 120),
        costText = "%.2f".format(item.cost),
        weight = "%.2f".format(item.weight),
        externalRefText = item.externalRef,
        durationMinutes = item.durationMinutes.toString(),
        thresholdText = "%.2f".format(item.threshold),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapDraftViews(items: MutableList<SwapDraft>): MutableList<SwapDraftView> {
    val views = mutableListOf<SwapDraftView>()
    for (item in items) {
        views.add(toSwapDraftView(item))
    }
    return views
}

fun sumSwapDraftView(view: SwapDraftView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedRetries.toDouble() + parsedFloorValue + parsedCost
}
