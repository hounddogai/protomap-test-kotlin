package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal declaration tier. */
data class SeasonalDeclarationTierView(
    var codeText: String,
    var title: String,
    var revision: String,
    var summary: String,
    var sequence: String,
    var statusLabel: String,
)

fun toSeasonalDeclarationTierView(item: SeasonalDeclarationTier): SeasonalDeclarationTierView {
    return SeasonalDeclarationTierView(
        codeText = item.code,
        title = item.title,
        revision = item.revision.toString(),
        summary = truncate(item.summary, 40),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDeclarationTierViews(items: MutableList<SeasonalDeclarationTier>): MutableList<SeasonalDeclarationTierView> {
    val views = mutableListOf<SeasonalDeclarationTierView>()
    for (item in items) {
        views.add(toSeasonalDeclarationTierView(item))
    }
    return views
}

fun sumSeasonalDeclarationTierView(view: SeasonalDeclarationTierView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedRevision.toDouble() + parsedSequence.toDouble()
}
