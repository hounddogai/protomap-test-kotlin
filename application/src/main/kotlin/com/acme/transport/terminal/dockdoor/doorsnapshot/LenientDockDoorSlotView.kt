package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dock door slot. */
data class LenientDockDoorSlotView(
    var codeText: String,
    var label: String,
    var versionText: String,
    var weight: String,
    var externalRefText: String,
    var archivedText: String,
    var segmentText: String,
    var loadKgText: String,
    var batchSizeText: String,
    var cost: String,
    var statusLabel: String,
)

fun toLenientDockDoorSlotView(item: LenientDockDoorSlot): LenientDockDoorSlotView {
    return LenientDockDoorSlotView(
        codeText = truncate(item.code, 64),
        label = item.label,
        versionText = item.version.toString(),
        weight = "%.2f".format(item.weight),
        externalRefText = truncate(item.externalRef, 80),
        archivedText = "${item.archived}",
        segmentText = item.segment,
        loadKgText = "%.2f".format(item.loadKg),
        batchSizeText = item.batchSize.toString(),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDockDoorSlotViews(items: MutableList<LenientDockDoorSlot>): MutableList<LenientDockDoorSlotView> {
    val views = mutableListOf<LenientDockDoorSlotView>()
    for (item in items) {
        views.add(toLenientDockDoorSlotView(item))
    }
    return views
}

fun sumLenientDockDoorSlotView(view: LenientDockDoorSlotView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedVersion.toDouble() + parsedWeight + parsedLoadKg
}
