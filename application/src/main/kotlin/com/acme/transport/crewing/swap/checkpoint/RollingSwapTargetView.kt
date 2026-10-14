package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling swap target. */
data class RollingSwapTargetView(
    var code: String,
    var nameText: String,
    var etaMinutesText: String,
    var routeCodeText: String,
    var archivedText: String,
    var checksum: String,
    var reviewer: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toRollingSwapTargetView(item: RollingSwapTarget): RollingSwapTargetView {
    return RollingSwapTargetView(
        code = item.code,
        nameText = item.name,
        etaMinutesText = item.etaMinutes.toString(),
        routeCodeText = truncate(item.routeCode, 40),
        archivedText = "${item.archived}",
        checksum = item.checksum,
        reviewer = item.reviewer,
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingSwapTargetViews(items: MutableList<RollingSwapTarget>): MutableList<RollingSwapTargetView> {
    val views = mutableListOf<RollingSwapTargetView>()
    for (item in items) {
        views.add(toRollingSwapTargetView(item))
    }
    return views
}

fun sumRollingSwapTargetView(view: RollingSwapTargetView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
