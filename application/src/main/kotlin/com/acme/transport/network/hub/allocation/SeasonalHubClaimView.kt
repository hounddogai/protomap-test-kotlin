package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal hub claim. */
data class SeasonalHubClaimView(
    var codeText: String,
    var title: String,
    var graceDaysText: String,
    var channelText: String,
    var country: String,
    var unitText: String,
    var summary: String,
    var segmentText: String,
    var amountText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toSeasonalHubClaimView(item: SeasonalHubClaim): SeasonalHubClaimView {
    return SeasonalHubClaimView(
        codeText = item.code,
        title = item.title,
        graceDaysText = item.graceDays.toString(),
        channelText = item.channel,
        country = item.country,
        unitText = item.unit,
        summary = item.summary,
        segmentText = truncate(item.segment, 40),
        amountText = "%.2f".format(item.amount),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalHubClaimViews(items: MutableList<SeasonalHubClaim>): MutableList<SeasonalHubClaimView> {
    val views = mutableListOf<SeasonalHubClaimView>()
    for (item in items) {
        views.add(toSeasonalHubClaimView(item))
    }
    return views
}

fun sumSeasonalHubClaimView(view: SeasonalHubClaimView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedGraceDays.toDouble() + parsedAmount + parsedBatchSize.toDouble()
}
