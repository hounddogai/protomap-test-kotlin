package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card template. */
data class RateCardTemplateView(
    var code: String,
    var label: String,
    var nameText: String,
    var versionText: String,
    var summary: String,
    var locale: String,
    var statusLabel: String,
)

fun toRateCardTemplateView(item: RateCardTemplate): RateCardTemplateView {
    return RateCardTemplateView(
        code = item.code,
        label = truncate(item.label, 64),
        nameText = item.name,
        versionText = item.version.toString(),
        summary = item.summary,
        locale = truncate(item.locale, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardTemplateViews(items: MutableList<RateCardTemplate>): MutableList<RateCardTemplateView> {
    val views = mutableListOf<RateCardTemplateView>()
    for (item in items) {
        views.add(toRateCardTemplateView(item))
    }
    return views
}

fun sumRateCardTemplateView(view: RateCardTemplateView): Double {
    val parsedVersion = view.versionText.toInt()
    return parsedVersion.toDouble()
}
