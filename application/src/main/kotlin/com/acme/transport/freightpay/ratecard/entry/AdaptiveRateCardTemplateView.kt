package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive rate card template. */
data class AdaptiveRateCardTemplateView(
    var code: String,
    var title: String,
    var createdAt: String,
    var capacity: String,
    var durationMinutes: String,
    var localeText: String,
    var statusLabel: String,
)

fun toAdaptiveRateCardTemplateView(item: AdaptiveRateCardTemplate): AdaptiveRateCardTemplateView {
    return AdaptiveRateCardTemplateView(
        code = truncate(item.code, 40),
        title = item.title,
        createdAt = item.createdAt.toString(),
        capacity = item.capacity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRateCardTemplateViews(items: MutableList<AdaptiveRateCardTemplate>): MutableList<AdaptiveRateCardTemplateView> {
    val views = mutableListOf<AdaptiveRateCardTemplateView>()
    for (item in items) {
        views.add(toAdaptiveRateCardTemplateView(item))
    }
    return views
}

fun sumAdaptiveRateCardTemplateView(view: AdaptiveRateCardTemplateView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedCreatedAt.toDouble() + parsedCapacity.toDouble() + parsedDurationMinutes.toDouble()
}
