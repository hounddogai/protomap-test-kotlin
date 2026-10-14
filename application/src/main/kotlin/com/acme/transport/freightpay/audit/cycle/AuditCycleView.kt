package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit cycle. */
data class AuditCycleView(
    var codeText: String,
    var name: String,
    var thresholdText: String,
    var startsAt: String,
    var depot: String,
    var quantity: String,
    var checksumText: String,
    var ownerId: String,
    var title: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toAuditCycleView(item: AuditCycle): AuditCycleView {
    return AuditCycleView(
        codeText = item.code,
        name = item.name,
        thresholdText = "%.2f".format(item.threshold),
        startsAt = item.startsAt.toString(),
        depot = truncate(item.depot, 40),
        quantity = item.quantity.toString(),
        checksumText = item.checksum,
        ownerId = item.ownerId,
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditCycleViews(items: MutableList<AuditCycle>): MutableList<AuditCycleView> {
    val views = mutableListOf<AuditCycleView>()
    for (item in items) {
        views.add(toAuditCycleView(item))
    }
    return views
}

fun sumAuditCycleView(view: AuditCycleView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedThreshold + parsedStartsAt.toDouble() + parsedQuantity.toDouble()
}
