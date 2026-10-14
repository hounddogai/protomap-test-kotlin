package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dispute plan. */
data class StrictDisputePlanView(
    var code: String,
    var title: String,
    var checksum: String,
    var laneText: String,
    var graceDays: String,
    var retries: String,
    var channel: String,
    var startsAtText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toStrictDisputePlanView(item: StrictDisputePlan): StrictDisputePlanView {
    return StrictDisputePlanView(
        code = truncate(item.code, 40),
        title = item.title,
        checksum = item.checksum,
        laneText = item.lane,
        graceDays = item.graceDays.toString(),
        retries = item.retries.toString(),
        channel = truncate(item.channel, 80),
        startsAtText = item.startsAt.toString(),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDisputePlanViews(items: MutableList<StrictDisputePlan>): MutableList<StrictDisputePlanView> {
    val views = mutableListOf<StrictDisputePlanView>()
    for (item in items) {
        views.add(toStrictDisputePlanView(item))
    }
    return views
}

fun sumStrictDisputePlanView(view: StrictDisputePlanView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedGraceDays.toDouble() + parsedRetries.toDouble() + parsedStartsAt.toDouble()
}
