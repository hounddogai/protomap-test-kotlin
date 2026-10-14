package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling hold allocation. */
data class RollingHoldAllocationView(
    var code: String,
    var title: String,
    var threshold: String,
    var revision: String,
    var driverId: String,
    var shiftCodeText: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toRollingHoldAllocationView(item: RollingHoldAllocation): RollingHoldAllocationView {
    return RollingHoldAllocationView(
        code = item.code,
        title = truncate(item.title, 120),
        threshold = "%.2f".format(item.threshold),
        revision = item.revision.toString(),
        driverId = truncate(item.driverId, 80),
        shiftCodeText = item.shiftCode,
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingHoldAllocationViews(items: MutableList<RollingHoldAllocation>): MutableList<RollingHoldAllocationView> {
    val views = mutableListOf<RollingHoldAllocationView>()
    for (item in items) {
        views.add(toRollingHoldAllocationView(item))
    }
    return views
}

fun sumRollingHoldAllocationView(view: RollingHoldAllocationView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedThreshold + parsedRevision.toDouble() + parsedRetries.toDouble()
}
