package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default redelivery notice. */
data class DefaultRedeliveryNoticeView(
    var code: String,
    var nameText: String,
    var capacity: String,
    var retries: String,
    var rate: String,
    var ownerIdText: String,
    var attempts: String,
    var discountText: String,
    var summary: String,
    var statusLabel: String,
)

fun toDefaultRedeliveryNoticeView(item: DefaultRedeliveryNotice): DefaultRedeliveryNoticeView {
    return DefaultRedeliveryNoticeView(
        code = truncate(item.code, 80),
        nameText = item.name,
        capacity = item.capacity.toString(),
        retries = item.retries.toString(),
        rate = "%.2f".format(item.rate),
        ownerIdText = truncate(item.ownerId, 80),
        attempts = item.attempts.toString(),
        discountText = "%.2f".format(item.discount),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRedeliveryNoticeViews(items: MutableList<DefaultRedeliveryNotice>): MutableList<DefaultRedeliveryNoticeView> {
    val views = mutableListOf<DefaultRedeliveryNoticeView>()
    for (item in items) {
        views.add(toDefaultRedeliveryNoticeView(item))
    }
    return views
}

fun sumDefaultRedeliveryNoticeView(view: DefaultRedeliveryNoticeView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedCapacity.toDouble() + parsedRetries.toDouble() + parsedRate
}
