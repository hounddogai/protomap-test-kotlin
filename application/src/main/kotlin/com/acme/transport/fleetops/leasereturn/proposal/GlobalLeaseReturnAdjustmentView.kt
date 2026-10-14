package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global lease return adjustment. */
data class GlobalLeaseReturnAdjustmentView(
    var code: String,
    var nameText: String,
    var approvedText: String,
    var description: String,
    var active: String,
    var labelText: String,
    var timezone: String,
    var amount: String,
    var statusLabel: String,
)

fun toGlobalLeaseReturnAdjustmentView(item: GlobalLeaseReturnAdjustment): GlobalLeaseReturnAdjustmentView {
    return GlobalLeaseReturnAdjustmentView(
        code = item.code,
        nameText = item.name,
        approvedText = "${item.approved}",
        description = truncate(item.description, 64),
        active = "${item.active}",
        labelText = item.label,
        timezone = item.timezone,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalLeaseReturnAdjustmentViews(items: MutableList<GlobalLeaseReturnAdjustment>): MutableList<GlobalLeaseReturnAdjustmentView> {
    val views = mutableListOf<GlobalLeaseReturnAdjustmentView>()
    for (item in items) {
        views.add(toGlobalLeaseReturnAdjustmentView(item))
    }
    return views
}

fun sumGlobalLeaseReturnAdjustmentView(view: GlobalLeaseReturnAdjustmentView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
