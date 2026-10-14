package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global redelivery override. */
data class GlobalRedeliveryOverrideView(
    var code: String,
    var label: String,
    var durationMinutes: String,
    var reviewerText: String,
    var discountText: String,
    var depot: String,
    var marginText: String,
    var channel: String,
    var threshold: String,
    var timezone: String,
    var visible: String,
    var statusLabel: String,
)

fun toGlobalRedeliveryOverrideView(item: GlobalRedeliveryOverride): GlobalRedeliveryOverrideView {
    return GlobalRedeliveryOverrideView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 80),
        durationMinutes = item.durationMinutes.toString(),
        reviewerText = truncate(item.reviewer, 80),
        discountText = "%.2f".format(item.discount),
        depot = truncate(item.depot, 40),
        marginText = "%.2f".format(item.margin),
        channel = truncate(item.channel, 120),
        threshold = "%.2f".format(item.threshold),
        timezone = item.timezone,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRedeliveryOverrideViews(items: MutableList<GlobalRedeliveryOverride>): MutableList<GlobalRedeliveryOverrideView> {
    val views = mutableListOf<GlobalRedeliveryOverrideView>()
    for (item in items) {
        views.add(toGlobalRedeliveryOverrideView(item))
    }
    return views
}

fun sumGlobalRedeliveryOverrideView(view: GlobalRedeliveryOverrideView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedDiscount + parsedMargin
}
