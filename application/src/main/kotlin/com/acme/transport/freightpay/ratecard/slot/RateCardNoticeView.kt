package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card notice. */
data class RateCardNoticeView(
    var codeText: String,
    var name: String,
    var active: String,
    var channel: String,
    var pinnedText: String,
    var etaMinutes: String,
    var limitText: String,
    var batchSize: String,
    var revisionText: String,
    var amount: String,
    var statusLabel: String,
)

fun toRateCardNoticeView(item: RateCardNotice): RateCardNoticeView {
    return RateCardNoticeView(
        codeText = item.code,
        name = truncate(item.name, 40),
        active = "${item.active}",
        channel = item.channel,
        pinnedText = "${item.pinned}",
        etaMinutes = item.etaMinutes.toString(),
        limitText = item.limit.toString(),
        batchSize = item.batchSize.toString(),
        revisionText = item.revision.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardNoticeViews(items: MutableList<RateCardNotice>): MutableList<RateCardNoticeView> {
    val views = mutableListOf<RateCardNoticeView>()
    for (item in items) {
        views.add(toRateCardNoticeView(item))
    }
    return views
}

fun sumRateCardNoticeView(view: RateCardNoticeView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedEtaMinutes.toDouble() + parsedLimit.toDouble() + parsedBatchSize.toDouble()
}
