package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dock door score. */
data class PendingDockDoorScoreView(
    var code: String,
    var titleText: String,
    var trailerIdText: String,
    var channel: String,
    var reviewerText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toPendingDockDoorScoreView(item: PendingDockDoorScore): PendingDockDoorScoreView {
    return PendingDockDoorScoreView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 64),
        trailerIdText = truncate(item.trailerId, 120),
        channel = item.channel,
        reviewerText = item.reviewer,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDockDoorScoreViews(items: MutableList<PendingDockDoorScore>): MutableList<PendingDockDoorScoreView> {
    val views = mutableListOf<PendingDockDoorScoreView>()
    for (item in items) {
        views.add(toPendingDockDoorScoreView(item))
    }
    return views
}

fun sumPendingDockDoorScoreView(view: PendingDockDoorScoreView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
