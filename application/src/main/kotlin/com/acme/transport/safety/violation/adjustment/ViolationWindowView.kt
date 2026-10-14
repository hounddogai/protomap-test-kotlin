package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation window. */
data class ViolationWindowView(
    var codeText: String,
    var name: String,
    var windowDays: String,
    var title: String,
    var threshold: String,
    var amount: String,
    var statusLabel: String,
)

fun toViolationWindowView(item: ViolationWindow): ViolationWindowView {
    return ViolationWindowView(
        codeText = truncate(item.code, 40),
        name = item.name,
        windowDays = item.windowDays.toString(),
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationWindowViews(items: MutableList<ViolationWindow>): MutableList<ViolationWindowView> {
    val views = mutableListOf<ViolationWindowView>()
    for (item in items) {
        views.add(toViolationWindowView(item))
    }
    return views
}

fun sumViolationWindowView(view: ViolationWindowView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedWindowDays.toDouble() + parsedThreshold + parsedAmount
}
