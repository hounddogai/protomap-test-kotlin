package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss adjustment. */
data class NearMissAdjustmentView(
    var code: String,
    var title: String,
    var amount: String,
    var archived: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toNearMissAdjustmentView(item: NearMissAdjustment): NearMissAdjustmentView {
    return NearMissAdjustmentView(
        code = item.code,
        title = truncate(item.title, 120),
        amount = "%.2f".format(item.amount),
        archived = "${item.archived}",
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissAdjustmentViews(items: MutableList<NearMissAdjustment>): MutableList<NearMissAdjustmentView> {
    val views = mutableListOf<NearMissAdjustmentView>()
    for (item in items) {
        views.add(toNearMissAdjustmentView(item))
    }
    return views
}

fun sumNearMissAdjustmentView(view: NearMissAdjustmentView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
