package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay entry. */
data class RelayEntryView(
    var codeText: String,
    var label: String,
    var contactEmail: String,
    var batchSize: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toRelayEntryView(item: RelayEntry): RelayEntryView {
    return RelayEntryView(
        codeText = item.code,
        label = truncate(item.label, 120),
        contactEmail = item.contactEmail,
        batchSize = item.batchSize.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayEntryViews(items: MutableList<RelayEntry>): MutableList<RelayEntryView> {
    val views = mutableListOf<RelayEntryView>()
    for (item in items) {
        views.add(toRelayEntryView(item))
    }
    return views
}

fun sumRelayEntryView(view: RelayEntryView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
