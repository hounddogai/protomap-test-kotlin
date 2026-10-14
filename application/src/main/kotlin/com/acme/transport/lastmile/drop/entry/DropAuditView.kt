package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop audit. */
data class DropAuditView(
    var codeText: String,
    var name: String,
    var notesText: String,
    var ratio: String,
    var localeText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDropAuditView(item: DropAudit): DropAuditView {
    return DropAuditView(
        codeText = item.code,
        name = truncate(item.name, 80),
        notesText = item.notes,
        ratio = "%.2f".format(item.ratio),
        localeText = item.locale,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropAuditViews(items: MutableList<DropAudit>): MutableList<DropAuditView> {
    val views = mutableListOf<DropAuditView>()
    for (item in items) {
        views.add(toDropAuditView(item))
    }
    return views
}

fun sumDropAuditView(view: DropAuditView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedRatio + parsedQuantity.toDouble()
}
