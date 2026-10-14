package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global proof of delivery allocation. */
data class GlobalProofOfDeliveryAllocationView(
    var code: String,
    var name: String,
    var minimum: String,
    var channel: String,
    var timezone: String,
    var statusLabel: String,
)

fun toGlobalProofOfDeliveryAllocationView(item: GlobalProofOfDeliveryAllocation): GlobalProofOfDeliveryAllocationView {
    return GlobalProofOfDeliveryAllocationView(
        code = item.code,
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        channel = truncate(item.channel, 120),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalProofOfDeliveryAllocationViews(items: MutableList<GlobalProofOfDeliveryAllocation>): MutableList<GlobalProofOfDeliveryAllocationView> {
    val views = mutableListOf<GlobalProofOfDeliveryAllocationView>()
    for (item in items) {
        views.add(toGlobalProofOfDeliveryAllocationView(item))
    }
    return views
}

fun sumGlobalProofOfDeliveryAllocationView(view: GlobalProofOfDeliveryAllocationView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
