package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dwell slot. */
data class StrictDwellSlotView(
    var code: String,
    var label: String,
    var dockText: String,
    var approvedText: String,
    var amount: String,
    var threshold: String,
    var carrier: String,
    var score: String,
    var attempts: String,
    var priority: String,
    var statusLabel: String,
)

fun toStrictDwellSlotView(item: StrictDwellSlot): StrictDwellSlotView {
    return StrictDwellSlotView(
        code = item.code,
        label = item.label,
        dockText = truncate(item.dock, 80),
        approvedText = "${item.approved}",
        amount = "%.2f".format(item.amount),
        threshold = "%.2f".format(item.threshold),
        carrier = truncate(item.carrier, 40),
        score = "%.2f".format(item.score),
        attempts = item.attempts.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDwellSlotViews(items: MutableList<StrictDwellSlot>): MutableList<StrictDwellSlotView> {
    val views = mutableListOf<StrictDwellSlotView>()
    for (item in items) {
        views.add(toStrictDwellSlotView(item))
    }
    return views
}

fun sumStrictDwellSlotView(view: StrictDwellSlotView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedAmount + parsedThreshold + parsedScore
}
