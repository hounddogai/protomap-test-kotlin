package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold claim. */
data class HoldClaimView(
    var codeText: String,
    var labelText: String,
    var channel: String,
    var marginText: String,
    var capacity: String,
    var checksumText: String,
    var ceiling: String,
    var name: String,
    var statusLabel: String,
)

fun toHoldClaimView(item: HoldClaim): HoldClaimView {
    return HoldClaimView(
        codeText = item.code,
        labelText = truncate(item.label, 120),
        channel = item.channel,
        marginText = "%.2f".format(item.margin),
        capacity = item.capacity.toString(),
        checksumText = item.checksum,
        ceiling = "%.2f".format(item.ceiling),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldClaimViews(items: MutableList<HoldClaim>): MutableList<HoldClaimView> {
    val views = mutableListOf<HoldClaimView>()
    for (item in items) {
        views.add(toHoldClaimView(item))
    }
    return views
}

fun sumHoldClaimView(view: HoldClaimView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedMargin + parsedCapacity.toDouble() + parsedCeiling
}
