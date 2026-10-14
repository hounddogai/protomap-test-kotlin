package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default near miss audit. */
data class DefaultNearMissAuditView(
    var code: String,
    var title: String,
    var amountText: String,
    var externalRefText: String,
    var pallets: String,
    var currency: String,
    var floorValueText: String,
    var shiftCode: String,
    var ownerIdText: String,
    var sourceText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toDefaultNearMissAuditView(item: DefaultNearMissAudit): DefaultNearMissAuditView {
    return DefaultNearMissAuditView(
        code = item.code,
        title = item.title,
        amountText = "%.2f".format(item.amount),
        externalRefText = item.externalRef,
        pallets = item.pallets.toString(),
        currency = item.currency,
        floorValueText = "%.2f".format(item.floorValue),
        shiftCode = item.shiftCode,
        ownerIdText = truncate(item.ownerId, 64),
        sourceText = item.source,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultNearMissAuditViews(items: MutableList<DefaultNearMissAudit>): MutableList<DefaultNearMissAuditView> {
    val views = mutableListOf<DefaultNearMissAuditView>()
    for (item in items) {
        views.add(toDefaultNearMissAuditView(item))
    }
    return views
}

fun sumDefaultNearMissAuditView(view: DefaultNearMissAuditView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedAmount + parsedPallets.toDouble() + parsedFloorValue
}
