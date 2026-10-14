package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer notice. */
data class TrailerNoticeView(
    var code: String,
    var name: String,
    var dock: String,
    var locale: String,
    var source: String,
    var amount: String,
    var statusLabel: String,
)

fun toTrailerNoticeView(item: TrailerNotice): TrailerNoticeView {
    return TrailerNoticeView(
        code = truncate(item.code, 40),
        name = item.name,
        dock = item.dock,
        locale = item.locale,
        source = truncate(item.source, 40),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerNoticeViews(items: MutableList<TrailerNotice>): MutableList<TrailerNoticeView> {
    val views = mutableListOf<TrailerNoticeView>()
    for (item in items) {
        views.add(toTrailerNoticeView(item))
    }
    return views
}

fun sumTrailerNoticeView(view: TrailerNoticeView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
