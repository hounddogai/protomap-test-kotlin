package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual clearance override. */
data class ManualClearanceOverrideView(
    var codeText: String,
    var name: String,
    var endsAt: String,
    var limitText: String,
    var costText: String,
    var ceiling: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toManualClearanceOverrideView(item: ManualClearanceOverride): ManualClearanceOverrideView {
    return ManualClearanceOverrideView(
        codeText = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        limitText = item.limit.toString(),
        costText = "%.2f".format(item.cost),
        ceiling = "%.2f".format(item.ceiling),
        sourceText = truncate(item.source, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualClearanceOverrideViews(items: MutableList<ManualClearanceOverride>): MutableList<ManualClearanceOverrideView> {
    val views = mutableListOf<ManualClearanceOverrideView>()
    for (item in items) {
        views.add(toManualClearanceOverrideView(item))
    }
    return views
}

fun sumManualClearanceOverrideView(view: ManualClearanceOverrideView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedEndsAt.toDouble() + parsedLimit.toDouble() + parsedCost
}
