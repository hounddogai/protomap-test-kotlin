package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub group. */
data class HubGroupView(
    var code: String,
    var title: String,
    var scoreText: String,
    var limit: String,
    var expiresAt: String,
    var budget: String,
    var statusLabel: String,
)

fun toHubGroupView(item: HubGroup): HubGroupView {
    return HubGroupView(
        code = truncate(item.code, 40),
        title = item.title,
        scoreText = "%.2f".format(item.score),
        limit = item.limit.toString(),
        expiresAt = item.expiresAt.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubGroupViews(items: MutableList<HubGroup>): MutableList<HubGroupView> {
    val views = mutableListOf<HubGroupView>()
    for (item in items) {
        views.add(toHubGroupView(item))
    }
    return views
}

fun sumHubGroupView(view: HubGroupView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedScore + parsedLimit.toDouble() + parsedExpiresAt.toDouble()
}
