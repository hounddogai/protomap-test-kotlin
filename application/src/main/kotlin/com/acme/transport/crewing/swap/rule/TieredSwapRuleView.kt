package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered swap rule. */
data class TieredSwapRuleView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var expiresAtText: String,
    var region: String,
    var updatedAt: String,
    var segment: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toTieredSwapRuleView(item: TieredSwapRule): TieredSwapRuleView {
    return TieredSwapRuleView(
        code = item.code,
        titleText = truncate(item.title, 120),
        etaMinutes = item.etaMinutes.toString(),
        expiresAtText = item.expiresAt.toString(),
        region = item.region,
        updatedAt = item.updatedAt.toString(),
        segment = truncate(item.segment, 120),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSwapRuleViews(items: MutableList<TieredSwapRule>): MutableList<TieredSwapRuleView> {
    val views = mutableListOf<TieredSwapRuleView>()
    for (item in items) {
        views.add(toTieredSwapRuleView(item))
    }
    return views
}

fun sumTieredSwapRuleView(view: TieredSwapRuleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble() + parsedUpdatedAt.toDouble()
}
