package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient rate card template. */
data class LenientRateCardTemplateView(
    var code: String,
    var label: String,
    var etaMinutesText: String,
    var batchSize: String,
    var ratioText: String,
    var source: String,
    var statusLabel: String,
)

fun toLenientRateCardTemplateView(item: LenientRateCardTemplate): LenientRateCardTemplateView {
    return LenientRateCardTemplateView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 80),
        etaMinutesText = item.etaMinutes.toString(),
        batchSize = item.batchSize.toString(),
        ratioText = "%.2f".format(item.ratio),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRateCardTemplateViews(items: MutableList<LenientRateCardTemplate>): MutableList<LenientRateCardTemplateView> {
    val views = mutableListOf<LenientRateCardTemplateView>()
    for (item in items) {
        views.add(toLenientRateCardTemplateView(item))
    }
    return views
}

fun sumLenientRateCardTemplateView(view: LenientRateCardTemplateView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedBatchSize.toDouble() + parsedRatio
}
