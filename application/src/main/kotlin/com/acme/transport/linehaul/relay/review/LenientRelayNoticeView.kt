package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient relay notice. */
data class LenientRelayNoticeView(
    var code: String,
    var labelText: String,
    var phone: String,
    var carrier: String,
    var approved: String,
    var maximumText: String,
    var segment: String,
    var rateText: String,
    var country: String,
    var retriesText: String,
    var locale: String,
    var statusLabel: String,
)

fun toLenientRelayNoticeView(item: LenientRelayNotice): LenientRelayNoticeView {
    return LenientRelayNoticeView(
        code = truncate(item.code, 80),
        labelText = item.label,
        phone = item.phone,
        carrier = item.carrier,
        approved = "${item.approved}",
        maximumText = "%.2f".format(item.maximum),
        segment = truncate(item.segment, 64),
        rateText = "%.2f".format(item.rate),
        country = item.country,
        retriesText = item.retries.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRelayNoticeViews(items: MutableList<LenientRelayNotice>): MutableList<LenientRelayNoticeView> {
    val views = mutableListOf<LenientRelayNoticeView>()
    for (item in items) {
        views.add(toLenientRelayNoticeView(item))
    }
    return views
}

fun sumLenientRelayNoticeView(view: LenientRelayNoticeView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedMaximum + parsedRate + parsedRetries.toDouble()
}
