package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery allocation. */
data class RedeliveryAllocationView(
    var codeText: String,
    var name: String,
    var ownerId: String,
    var channel: String,
    var pinned: String,
    var revision: String,
    var locale: String,
    var pallets: String,
    var graceDays: String,
    var description: String,
    var statusLabel: String,
)

fun toRedeliveryAllocationView(item: RedeliveryAllocation): RedeliveryAllocationView {
    return RedeliveryAllocationView(
        codeText = truncate(item.code, 40),
        name = item.name,
        ownerId = item.ownerId,
        channel = item.channel,
        pinned = "${item.pinned}",
        revision = item.revision.toString(),
        locale = truncate(item.locale, 80),
        pallets = item.pallets.toString(),
        graceDays = item.graceDays.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryAllocationViews(items: MutableList<RedeliveryAllocation>): MutableList<RedeliveryAllocationView> {
    val views = mutableListOf<RedeliveryAllocationView>()
    for (item in items) {
        views.add(toRedeliveryAllocationView(item))
    }
    return views
}

fun sumRedeliveryAllocationView(view: RedeliveryAllocationView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedRevision.toDouble() + parsedPallets.toDouble() + parsedGraceDays.toDouble()
}
