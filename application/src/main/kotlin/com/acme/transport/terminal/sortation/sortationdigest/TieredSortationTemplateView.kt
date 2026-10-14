package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered sortation template. */
data class TieredSortationTemplateView(
    var code: String,
    var name: String,
    var rate: String,
    var discount: String,
    var score: String,
    var contactEmail: String,
    var minimum: String,
    var retriesText: String,
    var budget: String,
    var quantity: String,
    var segment: String,
    var statusLabel: String,
)

fun toTieredSortationTemplateView(item: TieredSortationTemplate): TieredSortationTemplateView {
    return TieredSortationTemplateView(
        code = item.code,
        name = truncate(item.name, 40),
        rate = "%.2f".format(item.rate),
        discount = "%.2f".format(item.discount),
        score = "%.2f".format(item.score),
        contactEmail = truncate(item.contactEmail, 40),
        minimum = "%.2f".format(item.minimum),
        retriesText = item.retries.toString(),
        budget = "%.2f".format(item.budget),
        quantity = item.quantity.toString(),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSortationTemplateViews(items: MutableList<TieredSortationTemplate>): MutableList<TieredSortationTemplateView> {
    val views = mutableListOf<TieredSortationTemplateView>()
    for (item in items) {
        views.add(toTieredSortationTemplateView(item))
    }
    return views
}

fun sumTieredSortationTemplateView(view: TieredSortationTemplateView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRate + parsedDiscount + parsedScore
}
