package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped coaching notice. */
data class CappedCoachingNoticeView(
    var code: String,
    var name: String,
    var phone: String,
    var margin: String,
    var currency: String,
    var stopCount: String,
    var reviewer: String,
    var threshold: String,
    var statusLabel: String,
)

fun toCappedCoachingNoticeView(item: CappedCoachingNotice): CappedCoachingNoticeView {
    return CappedCoachingNoticeView(
        code = item.code,
        name = truncate(item.name, 40),
        phone = item.phone,
        margin = "%.2f".format(item.margin),
        currency = item.currency,
        stopCount = item.stopCount.toString(),
        reviewer = item.reviewer,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCoachingNoticeViews(items: MutableList<CappedCoachingNotice>): MutableList<CappedCoachingNoticeView> {
    val views = mutableListOf<CappedCoachingNoticeView>()
    for (item in items) {
        views.add(toCappedCoachingNoticeView(item))
    }
    return views
}

fun sumCappedCoachingNoticeView(view: CappedCoachingNoticeView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMargin + parsedStopCount.toDouble() + parsedThreshold
}
