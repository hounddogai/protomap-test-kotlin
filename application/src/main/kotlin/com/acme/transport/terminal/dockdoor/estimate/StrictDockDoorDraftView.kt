package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dock door draft. */
data class StrictDockDoorDraftView(
    var code: String,
    var label: String,
    var maximum: String,
    var contactEmail: String,
    var shiftCode: String,
    var phone: String,
    var cost: String,
    var createdAt: String,
    var approved: String,
    var statusLabel: String,
)

fun toStrictDockDoorDraftView(item: StrictDockDoorDraft): StrictDockDoorDraftView {
    return StrictDockDoorDraftView(
        code = item.code,
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        contactEmail = item.contactEmail,
        shiftCode = truncate(item.shiftCode, 64),
        phone = truncate(item.phone, 80),
        cost = "%.2f".format(item.cost),
        createdAt = item.createdAt.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDockDoorDraftViews(items: MutableList<StrictDockDoorDraft>): MutableList<StrictDockDoorDraftView> {
    val views = mutableListOf<StrictDockDoorDraftView>()
    for (item in items) {
        views.add(toStrictDockDoorDraftView(item))
    }
    return views
}

fun sumStrictDockDoorDraftView(view: StrictDockDoorDraftView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedMaximum + parsedCost + parsedCreatedAt.toDouble()
}
