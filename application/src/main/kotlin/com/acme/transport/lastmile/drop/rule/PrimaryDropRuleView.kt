package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary drop rule. */
data class PrimaryDropRuleView(
    var code: String,
    var label: String,
    var ceilingText: String,
    var maximumText: String,
    var scoreText: String,
    var checksum: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toPrimaryDropRuleView(item: PrimaryDropRule): PrimaryDropRuleView {
    return PrimaryDropRuleView(
        code = item.code,
        label = truncate(item.label, 120),
        ceilingText = "%.2f".format(item.ceiling),
        maximumText = "%.2f".format(item.maximum),
        scoreText = "%.2f".format(item.score),
        checksum = item.checksum,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDropRuleViews(items: MutableList<PrimaryDropRule>): MutableList<PrimaryDropRuleView> {
    val views = mutableListOf<PrimaryDropRuleView>()
    for (item in items) {
        views.add(toPrimaryDropRuleView(item))
    }
    return views
}

fun sumPrimaryDropRuleView(view: PrimaryDropRuleView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedCeiling + parsedMaximum + parsedScore
}
