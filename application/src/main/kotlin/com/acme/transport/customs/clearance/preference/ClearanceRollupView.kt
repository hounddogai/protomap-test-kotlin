package com.acme.transport.customs.clearance.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance rollup. */
data class ClearanceRollupView(
    var code: String,
    var nameText: String,
    var activeText: String,
    var description: String,
    var contactEmail: String,
    var countryText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toClearanceRollupView(item: ClearanceRollup): ClearanceRollupView {
    return ClearanceRollupView(
        code = item.code,
        nameText = item.name,
        activeText = "${item.active}",
        description = item.description,
        contactEmail = truncate(item.contactEmail, 120),
        countryText = item.country,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceRollupViews(items: MutableList<ClearanceRollup>): MutableList<ClearanceRollupView> {
    val views = mutableListOf<ClearanceRollupView>()
    for (item in items) {
        views.add(toClearanceRollupView(item))
    }
    return views
}

fun sumClearanceRollupView(view: ClearanceRollupView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
