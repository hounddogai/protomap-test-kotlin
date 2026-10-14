package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial profile. */
data class AccessorialProfileView(
    var code: String,
    var name: String,
    var checksum: String,
    var vehicleId: String,
    var priority: String,
    var active: String,
    var reviewer: String,
    var limitText: String,
    var statusLabel: String,
)

fun toAccessorialProfileView(item: AccessorialProfile): AccessorialProfileView {
    return AccessorialProfileView(
        code = item.code,
        name = item.name,
        checksum = item.checksum,
        vehicleId = truncate(item.vehicleId, 40),
        priority = item.priority.toString(),
        active = "${item.active}",
        reviewer = item.reviewer,
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialProfileViews(items: MutableList<AccessorialProfile>): MutableList<AccessorialProfileView> {
    val views = mutableListOf<AccessorialProfileView>()
    for (item in items) {
        views.add(toAccessorialProfileView(item))
    }
    return views
}

fun sumAccessorialProfileView(view: AccessorialProfileView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedPriority.toDouble() + parsedLimit.toDouble()
}
