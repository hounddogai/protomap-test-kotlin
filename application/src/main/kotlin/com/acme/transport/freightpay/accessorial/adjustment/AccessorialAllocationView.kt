package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial allocation. */
data class AccessorialAllocationView(
    var code: String,
    var title: String,
    var quantity: String,
    var localeText: String,
    var driverId: String,
    var reviewer: String,
    var labelText: String,
    var country: String,
    var versionText: String,
    var statusLabel: String,
)

fun toAccessorialAllocationView(item: AccessorialAllocation): AccessorialAllocationView {
    return AccessorialAllocationView(
        code = item.code,
        title = truncate(item.title, 64),
        quantity = item.quantity.toString(),
        localeText = item.locale,
        driverId = item.driverId,
        reviewer = item.reviewer,
        labelText = truncate(item.label, 80),
        country = truncate(item.country, 120),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialAllocationViews(items: MutableList<AccessorialAllocation>): MutableList<AccessorialAllocationView> {
    val views = mutableListOf<AccessorialAllocationView>()
    for (item in items) {
        views.add(toAccessorialAllocationView(item))
    }
    return views
}

fun sumAccessorialAllocationView(view: AccessorialAllocationView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedQuantity.toDouble() + parsedVersion.toDouble()
}
