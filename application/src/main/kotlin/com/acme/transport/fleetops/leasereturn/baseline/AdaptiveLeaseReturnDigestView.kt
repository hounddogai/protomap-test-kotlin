package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive lease return digest. */
data class AdaptiveLeaseReturnDigestView(
    var code: String,
    var title: String,
    var stopCountText: String,
    var label: String,
    var name: String,
    var phoneText: String,
    var vehicleId: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toAdaptiveLeaseReturnDigestView(item: AdaptiveLeaseReturnDigest): AdaptiveLeaseReturnDigestView {
    return AdaptiveLeaseReturnDigestView(
        code = item.code,
        title = item.title,
        stopCountText = item.stopCount.toString(),
        label = truncate(item.label, 80),
        name = item.name,
        phoneText = truncate(item.phone, 80),
        vehicleId = item.vehicleId,
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLeaseReturnDigestViews(items: MutableList<AdaptiveLeaseReturnDigest>): MutableList<AdaptiveLeaseReturnDigestView> {
    val views = mutableListOf<AdaptiveLeaseReturnDigestView>()
    for (item in items) {
        views.add(toAdaptiveLeaseReturnDigestView(item))
    }
    return views
}

fun sumAdaptiveLeaseReturnDigestView(view: AdaptiveLeaseReturnDigestView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedStopCount.toDouble() + parsedCeiling
}
