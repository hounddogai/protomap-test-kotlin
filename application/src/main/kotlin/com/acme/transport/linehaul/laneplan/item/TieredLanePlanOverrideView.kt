package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered lane plan override. */
data class TieredLanePlanOverrideView(
    var codeText: String,
    var title: String,
    var currency: String,
    var label: String,
    var category: String,
    var source: String,
    var loadKg: String,
    var sequenceText: String,
    var rate: String,
    var channel: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toTieredLanePlanOverrideView(item: TieredLanePlanOverride): TieredLanePlanOverrideView {
    return TieredLanePlanOverrideView(
        codeText = item.code,
        title = truncate(item.title, 64),
        currency = item.currency,
        label = item.label,
        category = item.category,
        source = truncate(item.source, 80),
        loadKg = "%.2f".format(item.loadKg),
        sequenceText = item.sequence.toString(),
        rate = "%.2f".format(item.rate),
        channel = truncate(item.channel, 64),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredLanePlanOverrideViews(items: MutableList<TieredLanePlanOverride>): MutableList<TieredLanePlanOverrideView> {
    val views = mutableListOf<TieredLanePlanOverrideView>()
    for (item in items) {
        views.add(toTieredLanePlanOverrideView(item))
    }
    return views
}

fun sumTieredLanePlanOverrideView(view: TieredLanePlanOverrideView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedLoadKg + parsedSequence.toDouble() + parsedRate
}
