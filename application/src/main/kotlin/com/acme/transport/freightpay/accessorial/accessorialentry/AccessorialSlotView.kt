package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial slot. */
data class AccessorialSlotView(
    var code: String,
    var title: String,
    var stopCountText: String,
    var createdAt: String,
    var summary: String,
    var visibleText: String,
    var amount: String,
    var reviewer: String,
    var externalRef: String,
    var score: String,
    var statusLabel: String,
)

fun toAccessorialSlotView(item: AccessorialSlot): AccessorialSlotView {
    return AccessorialSlotView(
        code = item.code,
        title = item.title,
        stopCountText = item.stopCount.toString(),
        createdAt = item.createdAt.toString(),
        summary = item.summary,
        visibleText = "${item.visible}",
        amount = "%.2f".format(item.amount),
        reviewer = item.reviewer,
        externalRef = truncate(item.externalRef, 80),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialSlotViews(items: MutableList<AccessorialSlot>): MutableList<AccessorialSlotView> {
    val views = mutableListOf<AccessorialSlotView>()
    for (item in items) {
        views.add(toAccessorialSlotView(item))
    }
    return views
}

fun sumAccessorialSlotView(view: AccessorialSlotView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedStopCount.toDouble() + parsedCreatedAt.toDouble() + parsedAmount
}
