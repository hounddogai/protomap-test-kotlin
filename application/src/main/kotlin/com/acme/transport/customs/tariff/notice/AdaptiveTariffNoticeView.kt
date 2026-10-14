package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tariff notice. */
data class AdaptiveTariffNoticeView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var endsAtText: String,
    var rate: String,
    var costText: String,
    var revision: String,
    var retries: String,
    var statusLabel: String,
)

fun toAdaptiveTariffNoticeView(item: AdaptiveTariffNotice): AdaptiveTariffNoticeView {
    return AdaptiveTariffNoticeView(
        code = truncate(item.code, 120),
        name = item.name,
        thresholdText = "%.2f".format(item.threshold),
        endsAtText = item.endsAt.toString(),
        rate = "%.2f".format(item.rate),
        costText = "%.2f".format(item.cost),
        revision = item.revision.toString(),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTariffNoticeViews(items: MutableList<AdaptiveTariffNotice>): MutableList<AdaptiveTariffNoticeView> {
    val views = mutableListOf<AdaptiveTariffNoticeView>()
    for (item in items) {
        views.add(toAdaptiveTariffNoticeView(item))
    }
    return views
}

fun sumAdaptiveTariffNoticeView(view: AdaptiveTariffNoticeView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedThreshold + parsedEndsAt.toDouble() + parsedRate
}
