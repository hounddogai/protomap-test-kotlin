package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal declaration batch. */
data class SeasonalDeclarationBatchView(
    var code: String,
    var title: String,
    var maximumText: String,
    var visibleText: String,
    var ownerId: String,
    var unitText: String,
    var depotText: String,
    var approved: String,
    var statusLabel: String,
)

fun toSeasonalDeclarationBatchView(item: SeasonalDeclarationBatch): SeasonalDeclarationBatchView {
    return SeasonalDeclarationBatchView(
        code = item.code,
        title = truncate(item.title, 80),
        maximumText = "%.2f".format(item.maximum),
        visibleText = "${item.visible}",
        ownerId = item.ownerId,
        unitText = item.unit,
        depotText = item.depot,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDeclarationBatchViews(items: MutableList<SeasonalDeclarationBatch>): MutableList<SeasonalDeclarationBatchView> {
    val views = mutableListOf<SeasonalDeclarationBatchView>()
    for (item in items) {
        views.add(toSeasonalDeclarationBatchView(item))
    }
    return views
}

fun sumSeasonalDeclarationBatchView(view: SeasonalDeclarationBatchView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMaximum
}
