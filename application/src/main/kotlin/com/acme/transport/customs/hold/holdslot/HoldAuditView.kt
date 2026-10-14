package com.acme.transport.customs.hold.holdslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold audit. */
data class HoldAuditView(
    var codeText: String,
    var name: String,
    var floorValue: String,
    var segment: String,
    var updatedAtText: String,
    var expiresAtText: String,
    var discount: String,
    var approved: String,
    var threshold: String,
    var amount: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toHoldAuditView(item: HoldAudit): HoldAuditView {
    return HoldAuditView(
        codeText = truncate(item.code, 64),
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        segment = truncate(item.segment, 120),
        updatedAtText = item.updatedAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        discount = "%.2f".format(item.discount),
        approved = "${item.approved}",
        threshold = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldAuditViews(items: MutableList<HoldAudit>): MutableList<HoldAuditView> {
    val views = mutableListOf<HoldAuditView>()
    for (item in items) {
        views.add(toHoldAuditView(item))
    }
    return views
}

fun sumHoldAuditView(view: HoldAuditView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedFloorValue + parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble()
}
