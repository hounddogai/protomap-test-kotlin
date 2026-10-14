package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy clearance target. */
data class LegacyClearanceTargetView(
    var codeText: String,
    var label: String,
    var localeText: String,
    var ratioText: String,
    var name: String,
    var priorityText: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toLegacyClearanceTargetView(item: LegacyClearanceTarget): LegacyClearanceTargetView {
    return LegacyClearanceTargetView(
        codeText = item.code,
        label = item.label,
        localeText = truncate(item.locale, 120),
        ratioText = "%.2f".format(item.ratio),
        name = truncate(item.name, 120),
        priorityText = item.priority.toString(),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyClearanceTargetViews(items: MutableList<LegacyClearanceTarget>): MutableList<LegacyClearanceTargetView> {
    val views = mutableListOf<LegacyClearanceTargetView>()
    for (item in items) {
        views.add(toLegacyClearanceTargetView(item))
    }
    return views
}

fun sumLegacyClearanceTargetView(view: LegacyClearanceTargetView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedRatio + parsedPriority.toDouble() + parsedPallets.toDouble()
}
