package com.acme.transport.emissions.idling.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling allocation. */
data class IdlingAllocationView(
    var codeText: String,
    var labelText: String,
    var countryText: String,
    var scoreText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toIdlingAllocationView(item: IdlingAllocation): IdlingAllocationView {
    return IdlingAllocationView(
        codeText = item.code,
        labelText = item.label,
        countryText = truncate(item.country, 120),
        scoreText = "%.2f".format(item.score),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingAllocationViews(items: MutableList<IdlingAllocation>): MutableList<IdlingAllocationView> {
    val views = mutableListOf<IdlingAllocationView>()
    for (item in items) {
        views.add(toIdlingAllocationView(item))
    }
    return views
}

fun sumIdlingAllocationView(view: IdlingAllocationView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
