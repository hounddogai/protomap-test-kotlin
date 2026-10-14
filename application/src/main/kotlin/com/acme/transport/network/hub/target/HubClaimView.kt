package com.acme.transport.network.hub.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub claim. */
data class HubClaimView(
    var code: String,
    var name: String,
    var batchSizeText: String,
    var limit: String,
    var summary: String,
    var unit: String,
    var statusLabel: String,
)

fun toHubClaimView(item: HubClaim): HubClaimView {
    return HubClaimView(
        code = item.code,
        name = item.name,
        batchSizeText = item.batchSize.toString(),
        limit = item.limit.toString(),
        summary = item.summary,
        unit = truncate(item.unit, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubClaimViews(items: MutableList<HubClaim>): MutableList<HubClaimView> {
    val views = mutableListOf<HubClaimView>()
    for (item in items) {
        views.add(toHubClaimView(item))
    }
    return views
}

fun sumHubClaimView(view: HubClaimView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedBatchSize.toDouble() + parsedLimit.toDouble()
}
