package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door rollup. */
data class DockDoorRollupView(
    var codeText: String,
    var labelText: String,
    var carrier: String,
    var active: String,
    var trailerId: String,
    var ownerId: String,
    var depot: String,
    var score: String,
    var statusLabel: String,
)

fun toDockDoorRollupView(item: DockDoorRollup): DockDoorRollupView {
    return DockDoorRollupView(
        codeText = item.code,
        labelText = item.label,
        carrier = item.carrier,
        active = "${item.active}",
        trailerId = truncate(item.trailerId, 40),
        ownerId = item.ownerId,
        depot = item.depot,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorRollupViews(items: MutableList<DockDoorRollup>): MutableList<DockDoorRollupView> {
    val views = mutableListOf<DockDoorRollupView>()
    for (item in items) {
        views.add(toDockDoorRollupView(item))
    }
    return views
}

fun sumDockDoorRollupView(view: DockDoorRollupView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
