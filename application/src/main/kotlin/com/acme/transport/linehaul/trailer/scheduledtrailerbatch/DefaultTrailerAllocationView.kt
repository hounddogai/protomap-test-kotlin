package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default trailer allocation. */
data class DefaultTrailerAllocationView(
    var codeText: String,
    var nameText: String,
    var country: String,
    var lane: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toDefaultTrailerAllocationView(item: DefaultTrailerAllocation): DefaultTrailerAllocationView {
    return DefaultTrailerAllocationView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        country = truncate(item.country, 120),
        lane = item.lane,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTrailerAllocationViews(items: MutableList<DefaultTrailerAllocation>): MutableList<DefaultTrailerAllocationView> {
    val views = mutableListOf<DefaultTrailerAllocationView>()
    for (item in items) {
        views.add(toDefaultTrailerAllocationView(item))
    }
    return views
}

fun sumDefaultTrailerAllocationView(view: DefaultTrailerAllocationView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
