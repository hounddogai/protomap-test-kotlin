package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss allocation. */
data class NearMissAllocationView(
    var code: String,
    var nameText: String,
    var pinnedText: String,
    var shiftCode: String,
    var maximum: String,
    var statusLabel: String,
)

fun toNearMissAllocationView(item: NearMissAllocation): NearMissAllocationView {
    return NearMissAllocationView(
        code = item.code,
        nameText = truncate(item.name, 80),
        pinnedText = "${item.pinned}",
        shiftCode = truncate(item.shiftCode, 80),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissAllocationViews(items: MutableList<NearMissAllocation>): MutableList<NearMissAllocationView> {
    val views = mutableListOf<NearMissAllocationView>()
    for (item in items) {
        views.add(toNearMissAllocationView(item))
    }
    return views
}

fun sumNearMissAllocationView(view: NearMissAllocationView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
