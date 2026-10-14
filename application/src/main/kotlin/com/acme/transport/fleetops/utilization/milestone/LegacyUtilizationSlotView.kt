package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy utilization slot. */
data class LegacyUtilizationSlotView(
    var codeText: String,
    var label: String,
    var externalRef: String,
    var rate: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toLegacyUtilizationSlotView(item: LegacyUtilizationSlot): LegacyUtilizationSlotView {
    return LegacyUtilizationSlotView(
        codeText = truncate(item.code, 64),
        label = item.label,
        externalRef = truncate(item.externalRef, 120),
        rate = "%.2f".format(item.rate),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyUtilizationSlotViews(items: MutableList<LegacyUtilizationSlot>): MutableList<LegacyUtilizationSlotView> {
    val views = mutableListOf<LegacyUtilizationSlotView>()
    for (item in items) {
        views.add(toLegacyUtilizationSlotView(item))
    }
    return views
}

fun sumLegacyUtilizationSlotView(view: LegacyUtilizationSlotView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedRate + parsedAttempts.toDouble()
}
