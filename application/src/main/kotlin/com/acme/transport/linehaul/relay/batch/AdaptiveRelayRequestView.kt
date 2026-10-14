package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive relay request. */
data class AdaptiveRelayRequestView(
    var codeText: String,
    var name: String,
    var currencyText: String,
    var active: String,
    var startsAt: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toAdaptiveRelayRequestView(item: AdaptiveRelayRequest): AdaptiveRelayRequestView {
    return AdaptiveRelayRequestView(
        codeText = truncate(item.code, 40),
        name = item.name,
        currencyText = item.currency,
        active = "${item.active}",
        startsAt = item.startsAt.toString(),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRelayRequestViews(items: MutableList<AdaptiveRelayRequest>): MutableList<AdaptiveRelayRequestView> {
    val views = mutableListOf<AdaptiveRelayRequestView>()
    for (item in items) {
        views.add(toAdaptiveRelayRequestView(item))
    }
    return views
}

fun sumAdaptiveRelayRequestView(view: AdaptiveRelayRequestView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedStartsAt.toDouble() + parsedSequence.toDouble()
}
