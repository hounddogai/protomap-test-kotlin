package com.acme.transport.lastmile.redelivery.threshold

/** How screens and exports show a redelivery notice. */
data class RedeliveryNoticeView(
    var code: String,
    var label: String,
    var minimum: String,
    var startsAt: String,
    var country: String,
    var statusLabel: String,
)

fun toRedeliveryNoticeView(item: RedeliveryNotice): RedeliveryNoticeView {
    return RedeliveryNoticeView(
        code = item.code,
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        startsAt = item.startsAt.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryNoticeViews(items: MutableList<RedeliveryNotice>): MutableList<RedeliveryNoticeView> {
    val views = mutableListOf<RedeliveryNoticeView>()
    for (item in items) {
        views.add(toRedeliveryNoticeView(item))
    }
    return views
}

fun sumRedeliveryNoticeView(view: RedeliveryNoticeView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMinimum + parsedStartsAt.toDouble()
}
