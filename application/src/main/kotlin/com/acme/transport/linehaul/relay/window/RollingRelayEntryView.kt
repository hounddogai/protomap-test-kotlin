package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling relay entry. */
data class RollingRelayEntryView(
    var codeText: String,
    var name: String,
    var reviewerText: String,
    var carrier: String,
    var source: String,
    var region: String,
    var attemptsText: String,
    var ceilingText: String,
    var lane: String,
    var externalRef: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toRollingRelayEntryView(item: RollingRelayEntry): RollingRelayEntryView {
    return RollingRelayEntryView(
        codeText = item.code,
        name = item.name,
        reviewerText = item.reviewer,
        carrier = truncate(item.carrier, 80),
        source = item.source,
        region = item.region,
        attemptsText = item.attempts.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        lane = item.lane,
        externalRef = truncate(item.externalRef, 40),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRelayEntryViews(items: MutableList<RollingRelayEntry>): MutableList<RollingRelayEntryView> {
    val views = mutableListOf<RollingRelayEntryView>()
    for (item in items) {
        views.add(toRollingRelayEntryView(item))
    }
    return views
}

fun sumRollingRelayEntryView(view: RollingRelayEntryView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedAttempts.toDouble() + parsedCeiling + parsedEtaMinutes.toDouble()
}
