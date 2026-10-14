package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient camera event policy. */
data class LenientCameraEventPolicyView(
    var code: String,
    var nameText: String,
    var margin: String,
    var channel: String,
    var reviewerText: String,
    var vehicleId: String,
    var phone: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toLenientCameraEventPolicyView(item: LenientCameraEventPolicy): LenientCameraEventPolicyView {
    return LenientCameraEventPolicyView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 64),
        margin = "%.2f".format(item.margin),
        channel = item.channel,
        reviewerText = item.reviewer,
        vehicleId = item.vehicleId,
        phone = truncate(item.phone, 40),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCameraEventPolicyViews(items: MutableList<LenientCameraEventPolicy>): MutableList<LenientCameraEventPolicyView> {
    val views = mutableListOf<LenientCameraEventPolicyView>()
    for (item in items) {
        views.add(toLenientCameraEventPolicyView(item))
    }
    return views
}

fun sumLenientCameraEventPolicyView(view: LenientCameraEventPolicyView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedMargin + parsedCeiling
}
