package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dock door exception. */
data class FallbackDockDoorExceptionView(
    var codeText: String,
    var name: String,
    var thresholdText: String,
    var retries: String,
    var reviewerText: String,
    var laneText: String,
    var marginText: String,
    var discount: String,
    var segment: String,
    var statusLabel: String,
)

fun toFallbackDockDoorExceptionView(item: FallbackDockDoorException): FallbackDockDoorExceptionView {
    return FallbackDockDoorExceptionView(
        codeText = item.code,
        name = truncate(item.name, 120),
        thresholdText = "%.2f".format(item.threshold),
        retries = item.retries.toString(),
        reviewerText = item.reviewer,
        laneText = truncate(item.lane, 40),
        marginText = "%.2f".format(item.margin),
        discount = "%.2f".format(item.discount),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDockDoorExceptionViews(items: MutableList<FallbackDockDoorException>): MutableList<FallbackDockDoorExceptionView> {
    val views = mutableListOf<FallbackDockDoorExceptionView>()
    for (item in items) {
        views.add(toFallbackDockDoorExceptionView(item))
    }
    return views
}

fun sumFallbackDockDoorExceptionView(view: FallbackDockDoorExceptionView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedThreshold + parsedRetries.toDouble() + parsedMargin
}
