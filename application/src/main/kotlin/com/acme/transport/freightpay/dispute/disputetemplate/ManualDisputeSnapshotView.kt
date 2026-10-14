package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual dispute snapshot. */
data class ManualDisputeSnapshotView(
    var code: String,
    var labelText: String,
    var durationMinutes: String,
    var unitText: String,
    var phone: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toManualDisputeSnapshotView(item: ManualDisputeSnapshot): ManualDisputeSnapshotView {
    return ManualDisputeSnapshotView(
        code = truncate(item.code, 80),
        labelText = item.label,
        durationMinutes = item.durationMinutes.toString(),
        unitText = item.unit,
        phone = item.phone,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDisputeSnapshotViews(items: MutableList<ManualDisputeSnapshot>): MutableList<ManualDisputeSnapshotView> {
    val views = mutableListOf<ManualDisputeSnapshotView>()
    for (item in items) {
        views.add(toManualDisputeSnapshotView(item))
    }
    return views
}

fun sumManualDisputeSnapshotView(view: ManualDisputeSnapshotView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble()
}
