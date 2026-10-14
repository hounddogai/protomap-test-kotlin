package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive sortation template. */
data class AdaptiveSortationTemplateView(
    var code: String,
    var title: String,
    var discount: String,
    var phone: String,
    var notesText: String,
    var locale: String,
    var carrierText: String,
    var reviewerText: String,
    var ceiling: String,
    var limit: String,
    var unit: String,
    var statusLabel: String,
)

fun toAdaptiveSortationTemplateView(item: AdaptiveSortationTemplate): AdaptiveSortationTemplateView {
    return AdaptiveSortationTemplateView(
        code = item.code,
        title = item.title,
        discount = "%.2f".format(item.discount),
        phone = truncate(item.phone, 80),
        notesText = item.notes,
        locale = item.locale,
        carrierText = item.carrier,
        reviewerText = truncate(item.reviewer, 120),
        ceiling = "%.2f".format(item.ceiling),
        limit = item.limit.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSortationTemplateViews(items: MutableList<AdaptiveSortationTemplate>): MutableList<AdaptiveSortationTemplateView> {
    val views = mutableListOf<AdaptiveSortationTemplateView>()
    for (item in items) {
        views.add(toAdaptiveSortationTemplateView(item))
    }
    return views
}

fun sumAdaptiveSortationTemplateView(view: AdaptiveSortationTemplateView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedDiscount + parsedCeiling + parsedLimit.toDouble()
}
