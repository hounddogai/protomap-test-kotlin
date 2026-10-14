package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub profile. */
data class HubProfileView(
    var code: String,
    var label: String,
    var unit: String,
    var expiresAt: String,
    var costText: String,
    var score: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toHubProfileView(item: HubProfile): HubProfileView {
    return HubProfileView(
        code = truncate(item.code, 120),
        label = item.label,
        unit = item.unit,
        expiresAt = item.expiresAt.toString(),
        costText = "%.2f".format(item.cost),
        score = "%.2f".format(item.score),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubProfileViews(items: MutableList<HubProfile>): MutableList<HubProfileView> {
    val views = mutableListOf<HubProfileView>()
    for (item in items) {
        views.add(toHubProfileView(item))
    }
    return views
}

fun sumHubProfileView(view: HubProfileView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedExpiresAt.toDouble() + parsedCost + parsedScore
}
