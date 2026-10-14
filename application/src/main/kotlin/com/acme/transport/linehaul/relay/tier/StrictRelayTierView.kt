package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict relay tier. */
data class StrictRelayTierView(
    var codeText: String,
    var label: String,
    var country: String,
    var pinnedText: String,
    var shiftCodeText: String,
    var sequenceText: String,
    var distanceKm: String,
    var batchSize: String,
    var locale: String,
    var retries: String,
    var statusLabel: String,
)

fun toStrictRelayTierView(item: StrictRelayTier): StrictRelayTierView {
    return StrictRelayTierView(
        codeText = item.code,
        label = truncate(item.label, 40),
        country = truncate(item.country, 80),
        pinnedText = "${item.pinned}",
        shiftCodeText = truncate(item.shiftCode, 64),
        sequenceText = item.sequence.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        batchSize = item.batchSize.toString(),
        locale = item.locale,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRelayTierViews(items: MutableList<StrictRelayTier>): MutableList<StrictRelayTierView> {
    val views = mutableListOf<StrictRelayTierView>()
    for (item in items) {
        views.add(toStrictRelayTierView(item))
    }
    return views
}

fun sumStrictRelayTierView(view: StrictRelayTierView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedSequence.toDouble() + parsedDistanceKm + parsedBatchSize.toDouble()
}
