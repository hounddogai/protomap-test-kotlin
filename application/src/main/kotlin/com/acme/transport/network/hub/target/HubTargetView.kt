package com.acme.transport.network.hub.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub target. */
data class HubTargetView(
    var code: String,
    var title: String,
    var etaMinutes: String,
    var routeCode: String,
    var externalRef: String,
    var expiresAt: String,
    var dock: String,
    var statusLabel: String,
)

fun toHubTargetView(item: HubTarget): HubTargetView {
    return HubTargetView(
        code = item.code,
        title = item.title,
        etaMinutes = item.etaMinutes.toString(),
        routeCode = truncate(item.routeCode, 120),
        externalRef = truncate(item.externalRef, 80),
        expiresAt = item.expiresAt.toString(),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubTargetViews(items: MutableList<HubTarget>): MutableList<HubTargetView> {
    val views = mutableListOf<HubTargetView>()
    for (item in items) {
        views.add(toHubTargetView(item))
    }
    return views
}

fun sumHubTargetView(view: HubTargetView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble()
}
