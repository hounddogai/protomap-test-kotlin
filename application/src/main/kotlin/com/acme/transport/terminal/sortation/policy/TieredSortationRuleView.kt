package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered sortation rule. */
data class TieredSortationRuleView(
    var code: String,
    var title: String,
    var sequenceText: String,
    var sourceText: String,
    var batchSize: String,
    var channel: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toTieredSortationRuleView(item: TieredSortationRule): TieredSortationRuleView {
    return TieredSortationRuleView(
        code = item.code,
        title = truncate(item.title, 40),
        sequenceText = item.sequence.toString(),
        sourceText = item.source,
        batchSize = item.batchSize.toString(),
        channel = truncate(item.channel, 64),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSortationRuleViews(items: MutableList<TieredSortationRule>): MutableList<TieredSortationRuleView> {
    val views = mutableListOf<TieredSortationRuleView>()
    for (item in items) {
        views.add(toTieredSortationRuleView(item))
    }
    return views
}

fun sumTieredSortationRuleView(view: TieredSortationRuleView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedSequence.toDouble() + parsedBatchSize.toDouble()
}
