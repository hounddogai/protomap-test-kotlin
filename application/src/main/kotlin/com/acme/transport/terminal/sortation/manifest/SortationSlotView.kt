package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation slot. */
data class SortationSlotView(
    var codeText: String,
    var labelText: String,
    var stopCount: String,
    var loadKg: String,
    var locale: String,
    var statusLabel: String,
)

fun toSortationSlotView(item: SortationSlot): SortationSlotView {
    return SortationSlotView(
        codeText = truncate(item.code, 80),
        labelText = item.label,
        stopCount = item.stopCount.toString(),
        loadKg = "%.2f".format(item.loadKg),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationSlotViews(items: MutableList<SortationSlot>): MutableList<SortationSlotView> {
    val views = mutableListOf<SortationSlotView>()
    for (item in items) {
        views.add(toSortationSlotView(item))
    }
    return views
}

fun sumSortationSlotView(view: SortationSlotView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedStopCount.toDouble() + parsedLoadKg
}
