package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender group. */
data class TenderGroupView(
    var code: String,
    var title: String,
    var ceiling: String,
    var startsAtText: String,
    var discountText: String,
    var channel: String,
    var ratioText: String,
    var priority: String,
    var limit: String,
    var weightText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toTenderGroupView(item: TenderGroup): TenderGroupView {
    return TenderGroupView(
        code = item.code,
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        startsAtText = item.startsAt.toString(),
        discountText = "%.2f".format(item.discount),
        channel = item.channel,
        ratioText = "%.2f".format(item.ratio),
        priority = item.priority.toString(),
        limit = item.limit.toString(),
        weightText = "%.2f".format(item.weight),
        timezone = truncate(item.timezone, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderGroupViews(items: MutableList<TenderGroup>): MutableList<TenderGroupView> {
    val views = mutableListOf<TenderGroupView>()
    for (item in items) {
        views.add(toTenderGroupView(item))
    }
    return views
}

fun sumTenderGroupView(view: TenderGroupView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedCeiling + parsedStartsAt.toDouble() + parsedDiscount
}
