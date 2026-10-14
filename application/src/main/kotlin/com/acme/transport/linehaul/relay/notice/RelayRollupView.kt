package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay rollup. */
data class RelayRollupView(
    var code: String,
    var title: String,
    var version: String,
    var description: String,
    var palletsText: String,
    var attempts: String,
    var locale: String,
    var expiresAtText: String,
    var budgetText: String,
    var ratio: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toRelayRollupView(item: RelayRollup): RelayRollupView {
    return RelayRollupView(
        code = item.code,
        title = item.title,
        version = item.version.toString(),
        description = item.description,
        palletsText = item.pallets.toString(),
        attempts = item.attempts.toString(),
        locale = truncate(item.locale, 80),
        expiresAtText = item.expiresAt.toString(),
        budgetText = "%.2f".format(item.budget),
        ratio = "%.2f".format(item.ratio),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayRollupViews(items: MutableList<RelayRollup>): MutableList<RelayRollupView> {
    val views = mutableListOf<RelayRollupView>()
    for (item in items) {
        views.add(toRelayRollupView(item))
    }
    return views
}

fun sumRelayRollupView(view: RelayRollupView): Double {
    val parsedVersion = view.version.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedVersion.toDouble() + parsedPallets.toDouble() + parsedAttempts.toDouble()
}
