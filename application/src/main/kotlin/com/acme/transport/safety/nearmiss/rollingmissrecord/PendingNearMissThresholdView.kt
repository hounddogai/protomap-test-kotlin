package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending near miss threshold. */
data class PendingNearMissThresholdView(
    var code: String,
    var name: String,
    var cost: String,
    var visible: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toPendingNearMissThresholdView(item: PendingNearMissThreshold): PendingNearMissThresholdView {
    return PendingNearMissThresholdView(
        code = truncate(item.code, 120),
        name = item.name,
        cost = "%.2f".format(item.cost),
        visible = "${item.visible}",
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingNearMissThresholdViews(items: MutableList<PendingNearMissThreshold>): MutableList<PendingNearMissThresholdView> {
    val views = mutableListOf<PendingNearMissThresholdView>()
    for (item in items) {
        views.add(toPendingNearMissThresholdView(item))
    }
    return views
}

fun sumPendingNearMissThresholdView(view: PendingNearMissThresholdView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}
