package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door claim. */
data class DockDoorClaimView(
    var code: String,
    var title: String,
    var summary: String,
    var approved: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toDockDoorClaimView(item: DockDoorClaim): DockDoorClaimView {
    return DockDoorClaimView(
        code = item.code,
        title = truncate(item.title, 64),
        summary = truncate(item.summary, 40),
        approved = "${item.approved}",
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorClaimViews(items: MutableList<DockDoorClaim>): MutableList<DockDoorClaimView> {
    val views = mutableListOf<DockDoorClaimView>()
    for (item in items) {
        views.add(toDockDoorClaimView(item))
    }
    return views
}

fun sumDockDoorClaimView(view: DockDoorClaimView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
