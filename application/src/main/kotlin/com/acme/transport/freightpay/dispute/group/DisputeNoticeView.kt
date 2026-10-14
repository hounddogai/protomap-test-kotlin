package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute notice. */
data class DisputeNoticeView(
    var code: String,
    var name: String,
    var description: String,
    var weightText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toDisputeNoticeView(item: DisputeNotice): DisputeNoticeView {
    return DisputeNoticeView(
        code = item.code,
        name = item.name,
        description = item.description,
        weightText = "%.2f".format(item.weight),
        ownerId = truncate(item.ownerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeNoticeViews(items: MutableList<DisputeNotice>): MutableList<DisputeNoticeView> {
    val views = mutableListOf<DisputeNoticeView>()
    for (item in items) {
        views.add(toDisputeNoticeView(item))
    }
    return views
}

fun sumDisputeNoticeView(view: DisputeNoticeView): Double {
    val parsedWeight = view.weightText.toDouble()
    return parsedWeight
}
