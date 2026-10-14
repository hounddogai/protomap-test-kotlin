package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial tier. */
data class AccessorialTierView(
    var code: String,
    var label: String,
    var ownerId: String,
    var carrier: String,
    var etaMinutes: String,
    var shiftCode: String,
    var capacity: String,
    var statusLabel: String,
)

fun toAccessorialTierView(item: AccessorialTier): AccessorialTierView {
    return AccessorialTierView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 80),
        ownerId = truncate(item.ownerId, 120),
        carrier = truncate(item.carrier, 120),
        etaMinutes = item.etaMinutes.toString(),
        shiftCode = item.shiftCode,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialTierViews(items: MutableList<AccessorialTier>): MutableList<AccessorialTierView> {
    val views = mutableListOf<AccessorialTierView>()
    for (item in items) {
        views.add(toAccessorialTierView(item))
    }
    return views
}

fun sumAccessorialTierView(view: AccessorialTierView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedEtaMinutes.toDouble() + parsedCapacity.toDouble()
}
