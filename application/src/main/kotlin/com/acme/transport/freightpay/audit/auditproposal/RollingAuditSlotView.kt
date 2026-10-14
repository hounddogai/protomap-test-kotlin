package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling audit slot. */
data class RollingAuditSlotView(
    var code: String,
    var name: String,
    var createdAt: String,
    var weightText: String,
    var carrier: String,
    var versionText: String,
    var statusLabel: String,
)

fun toRollingAuditSlotView(item: RollingAuditSlot): RollingAuditSlotView {
    return RollingAuditSlotView(
        code = truncate(item.code, 120),
        name = item.name,
        createdAt = item.createdAt.toString(),
        weightText = "%.2f".format(item.weight),
        carrier = truncate(item.carrier, 40),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAuditSlotViews(items: MutableList<RollingAuditSlot>): MutableList<RollingAuditSlotView> {
    val views = mutableListOf<RollingAuditSlotView>()
    for (item in items) {
        views.add(toRollingAuditSlotView(item))
    }
    return views
}

fun sumRollingAuditSlotView(view: RollingAuditSlotView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedCreatedAt.toDouble() + parsedWeight + parsedVersion.toDouble()
}
