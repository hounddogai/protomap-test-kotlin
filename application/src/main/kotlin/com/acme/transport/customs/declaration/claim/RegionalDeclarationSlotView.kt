package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional declaration slot. */
data class RegionalDeclarationSlotView(
    var code: String,
    var label: String,
    var palletsText: String,
    var priorityText: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toRegionalDeclarationSlotView(item: RegionalDeclarationSlot): RegionalDeclarationSlotView {
    return RegionalDeclarationSlotView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 80),
        palletsText = item.pallets.toString(),
        priorityText = item.priority.toString(),
        externalRef = truncate(item.externalRef, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDeclarationSlotViews(items: MutableList<RegionalDeclarationSlot>): MutableList<RegionalDeclarationSlotView> {
    val views = mutableListOf<RegionalDeclarationSlotView>()
    for (item in items) {
        views.add(toRegionalDeclarationSlotView(item))
    }
    return views
}

fun sumRegionalDeclarationSlotView(view: RegionalDeclarationSlotView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedPallets.toDouble() + parsedPriority.toDouble()
}
