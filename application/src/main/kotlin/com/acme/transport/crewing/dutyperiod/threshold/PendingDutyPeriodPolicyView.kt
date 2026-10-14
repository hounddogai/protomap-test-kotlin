package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending duty period policy. */
data class PendingDutyPeriodPolicyView(
    var codeText: String,
    var title: String,
    var vehicleId: String,
    var shiftCode: String,
    var phoneText: String,
    var ratioText: String,
    var quantity: String,
    var dockText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toPendingDutyPeriodPolicyView(item: PendingDutyPeriodPolicy): PendingDutyPeriodPolicyView {
    return PendingDutyPeriodPolicyView(
        codeText = item.code,
        title = item.title,
        vehicleId = item.vehicleId,
        shiftCode = item.shiftCode,
        phoneText = truncate(item.phone, 80),
        ratioText = "%.2f".format(item.ratio),
        quantity = item.quantity.toString(),
        dockText = truncate(item.dock, 120),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDutyPeriodPolicyViews(items: MutableList<PendingDutyPeriodPolicy>): MutableList<PendingDutyPeriodPolicyView> {
    val views = mutableListOf<PendingDutyPeriodPolicyView>()
    for (item in items) {
        views.add(toPendingDutyPeriodPolicyView(item))
    }
    return views
}

fun sumPendingDutyPeriodPolicyView(view: PendingDutyPeriodPolicyView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedRatio + parsedQuantity.toDouble() + parsedStopCount.toDouble()
}
